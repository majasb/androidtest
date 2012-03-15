package bratseth.maja.msgtransport.transport.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import bratseth.maja.androidtest.service.*;
import bratseth.maja.msgtransport.transport.Invocation;
import bratseth.maja.msgtransport.transport.InvocationResult;
import bratseth.maja.msgtransport.transport.TransportMessages;

public class MessengerServer extends Handler implements CallbackHandler {

    private final String tag = MessengerServer.class.getSimpleName();

    private final Map<Class, List<Messenger>> callbackListenerClients = new HashMap<Class, List<Messenger>>();

    private ServiceLocator serviceLocator;

    public void setServiceLocator(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    @Override
    public void handleMessage(Message message) {
        try {
            if (message.replyTo == null) {
                throw new IllegalArgumentException("No replyTo in " + toLogString(message));
            }
            if (message.what == TransportMessages.MSG_REGISTER_LISTENER) {
                registerListener(message);
            } else if (message.what == TransportMessages.MSG_UNREGISTER_LISTENER) {
                unregisterListener(message);
            } else if (message.what == TransportMessages.MSG_INVOKE) {
                handleInvocation(message);
            } else {
                throw new IllegalArgumentException("Unexpected message " + toLogString(message));
            }
        } catch (Throwable e) {
            try {
                Log.e(tag, "Caught exception. Passing on to client. Data: " + toLogString(message), e);
                Message replyMessage = createReply(message, InvocationResult.exception(e));
                message.replyTo.send(replyMessage);
            } catch (Exception e2) {
                Log.e(tag, "Caught exception and could not send it as reply", e);
            }
        }
    }

    private String toLogString(Message message) {
        return message.what + ": " + message.getData();
    }

    private void handleInvocation(Message message) throws Throwable {
        Invocation invocation = TransportMessages.extractInvocation(message);
        Object result = invokeService(invocation);
        Message replyMessage = createReply(message, InvocationResult.normalResult(result));
        message.replyTo.send(replyMessage);
    }

    private void registerListener(Message message) {
        final Class eventType = TransportMessages.extractEventType(message);
        List<Messenger> messengers = callbackListenerClients.get(eventType);
        if (messengers == null) {
            messengers = new ArrayList<Messenger>();
            callbackListenerClients.put(eventType, messengers);
        }
        messengers.add(message.replyTo);
    }
    
    private void unregisterListener(Message message) {
        final Class eventType = TransportMessages.extractEventType(message);
        List<Messenger> messengers = callbackListenerClients.get(eventType);
        if (messengers != null) {
            messengers.remove(message.replyTo);
            if (messengers.isEmpty()) {
                callbackListenerClients.remove(eventType);
            }
        }
    }

    public void sendCallback(CallbackEvent callback) {
        if (callbackListenerClients.containsKey(callback.getClass())) {
            for (Messenger client : callbackListenerClients.get(callback.getClass())) {
                Message callbackMessage = TransportMessages.createCallback(callback);
                try {
                    client.send(callbackMessage);
                }
                catch (RemoteException e) {
                    // TODO: Maybe remove client
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private Message createReply(Message invocationMessage, InvocationResult result) {
        return TransportMessages.createInvocationReply(invocationMessage, result);
    }

    private Object invokeService(Invocation invocation) throws Throwable {
        Method method = getMethod(invocation);
        Object service = getService(invocation.getServiceType());
        try {
            final Class[] parameterTypes = invocation.getParameterClasses();
            if (parameterTypes.length > 0
                && ExceptionHandler.class.isAssignableFrom(parameterTypes[parameterTypes.length - 1])) {

                ResultHandlerStub resultHandlerStub = new ResultHandlerStub();
                Object[] modifiedParameters = new Object[parameterTypes.length];
                System.arraycopy(invocation.getParameters(), 0, modifiedParameters, 0, parameterTypes.length - 1);
                modifiedParameters[parameterTypes.length - 1] = resultHandlerStub;

                method.invoke(service, modifiedParameters);
                if (!resultHandlerStub.hasBeenSet()) {
                    throw new IllegalStateException("Expected a result from invocation " + invocation);
                }
                if (resultHandlerStub.isException()) {
                    throw resultHandlerStub.getException();
                }
                return resultHandlerStub.getResult();
            } else {
                return method.invoke(service, invocation.getParameters());
            }
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private Object getService(Class serviceType) {
        final Object service = serviceLocator.locate(serviceType);
        if (service == null) {
            throw new IllegalArgumentException("No such service: " + serviceType);
        }
        return service;
    }

    private Method getMethod(Invocation invocation) throws Exception {
        return invocation.getServiceType().getMethod(invocation.getMethodName(), invocation.getParameterClasses());
    }

}
