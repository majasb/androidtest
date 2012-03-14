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

public class ServiceInvokerMessageHandler extends Handler implements CallbackHandler {

    private final String tag = ServiceInvokerMessageHandler.class.getSimpleName();

    private final Map<Class, List<Messenger>> callbackListenerClients = new HashMap<Class, List<Messenger>>();

    private ServiceLocator serviceLocator;

    public void setServiceLocator(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    @Override
    public void handleMessage(Message message) {
        try {
            if (message.replyTo == null) {
                throw new IllegalArgumentException("No replyTo in " + message.getData());
            }
            if (message.getData().containsKey("registerListener")) {
                registerListener(message);
            } else if (message.getData().containsKey("unregisterListener")) {
                unregisterListener(message);
            } else if (message.getData().containsKey("invocation")) {
                handleInvocation(message);
            } else {
                throw new IllegalArgumentException("Unexpected message " + message.getData());
            }
        } catch (Throwable e) {
            try {
                Log.e(tag, "Caught exception. Passing on to client. Data: " + message.getData(), e);
                Message replyMessage = createReply(message, InvocationResult.exception(e));
                message.replyTo.send(replyMessage);
            } catch (Exception e2) {
                final String msg = "Caught exception and could not send it as reply. Time: " + System.currentTimeMillis();
                Log.e(tag, msg, e);
            }
        }
    }

    private void handleInvocation(Message message) throws Throwable {
        Invocation invocation = (Invocation) message.getData().getSerializable("invocation");
        Object result = invokeService(invocation);
        Message replyMessage = createReply(message, InvocationResult.normalResult(result));
        message.replyTo.send(replyMessage);
    }

    private void registerListener(Message message) {
        final Class eventType = (Class) message.getData().getSerializable("eventType");
        List<Messenger> messengers = callbackListenerClients.get(eventType);
        if (messengers == null) {
            messengers = new ArrayList<Messenger>();
            callbackListenerClients.put(eventType, messengers);
        }
        messengers.add(message.replyTo);
    }
    
    private void unregisterListener(Message message) {
        final Class eventType = (Class) message.getData().getSerializable("eventType");
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
                Message event = Message.obtain();
                event.getData().putSerializable("event", callback);
                try {
                    client.send(event);
                }
                catch (RemoteException e) {
                    // TODO: Maybe remove client
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private Message createReply(Message invocationMessage, InvocationResult result) {
        Message replyMessage = Message.obtain();
        replyMessage.getData().putSerializable("result", result);
        replyMessage.getData().putLong("resultHandlerId", invocationMessage.getData().getLong("resultHandlerId"));
        return replyMessage;
    }

    private Object invokeService(Invocation invocation) throws Throwable {
        Method method = findMethod(invocation);
        Object service = getService(invocation.getServiceType());
        try {
            // only supports one resulthandler/exceptionhandler for now
            final Class[] parameterClasses = invocation.getParameterClasses();
            if (parameterClasses.length > 0
                && ExceptionHandler.class.isAssignableFrom(parameterClasses[parameterClasses.length - 1])) {

                Class lastParameterType = parameterClasses[parameterClasses.length - 1];
                ResultHandlerStub resultHandlerStub = new ResultHandlerStub();
                Object[] modifiedParameters = new Object[parameterClasses.length];
                System.arraycopy(invocation.getParameters(), 0, modifiedParameters, 0, parameterClasses.length - 1);
                modifiedParameters[parameterClasses.length - 1] = resultHandlerStub;
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

    private Method findMethod(Invocation invocation) throws Exception {
        return invocation.getServiceType().getMethod(invocation.getMethodName(), invocation.getParameterClasses());
    }

}
