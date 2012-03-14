package bratseth.maja.msgtransport.transport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import bratseth.maja.androidtest.service.*;

public class ServiceInvokerMessageHandler extends Handler implements CallbackHandler {

    private final String tag = ServiceInvokerMessageHandler.class.getSimpleName();

    private Serializer serializer;
    private ServiceLocator serviceLocator;

    private final Map<Class, List<Messenger>> callbackListenerClients = new HashMap<Class, List<Messenger>>();

    @Override
    public void handleMessage(Message message) {
        try {
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
        log("Got message: " + invocation);
        Object result = invokeService(message, invocation);
        Message replyMessage = createReply(message, InvocationResult.normalResult(result));
        message.replyTo.send(replyMessage);
    }

    private void registerListener(Message message) {
        if (message.replyTo == null) {
            throw new IllegalArgumentException("No replyTo in " + message.getData());
        }
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

    private void log(String msg) {
        Log.i(ServiceInvokerMessageHandler.class.getSimpleName(), msg);
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public void setServiceLocator(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    private Object invokeService(Message message, Invocation invocation) throws Throwable {
        Method method = findMethod(invocation);
        Object service = findService(invocation.getServiceType());
        if (service == null) {
            throw new IllegalArgumentException("No such service: " + invocation.getServiceType());
        }
        try {
            final Object[] parameters = invocation.getParameters();
            // only supports one resulthandler for now
            if (parameters.length > 0) {
                Object parameter = parameters[parameters.length - 1];
                if (parameter instanceof ResultHandlerStub) {
                    ResultHandlerStub resultHandlerStub = (ResultHandlerStub) parameter;
                    method.invoke(service, parameters);
                    return resultHandlerStub.getResult(); // TODO: remove exception field, and don't need to send from client
                }
                if (parameter instanceof CallbackListenerStub) {
                    if (parameters.length > 1) {
                        throw new IllegalArgumentException("Only one callback listener parameter supported");
                    }
                    CallbackListenerStub callbackListener = (CallbackListenerStub) parameter;
                    Object listenerProxy =
                        createListenerProxy(method.getParameterTypes()[0], message, callbackListener);
                    method.invoke(service, listenerProxy);
                    return null;
                }
            }
            return method.invoke(service, parameters);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private Object createListenerProxy(final Class callbackListenerType, final Message registerCallbackMessage,
                                       final CallbackListenerStub listenerStub) {
        InvocationHandler invocationHandler =
            new CallbackListenerInvocationHandler(callbackListenerType,
                                                  registerCallbackMessage.replyTo,
                                                  listenerStub,
                                                  registerCallbackMessage.getData().getLong("resultHandlerId"));
        return Proxy.newProxyInstance(callbackListenerType.getClassLoader(),
                                      new Class[]{callbackListenerType},
                                      invocationHandler);
    }

    private Object findService(Class serviceType) {
        return serviceLocator.locate(serviceType);
    }

    private Method findMethod(Invocation invocation) throws Exception {
        return invocation.getServiceType().getMethod(invocation.getMethodName(), invocation.getParameterClasses());
    }

    private class CallbackListenerInvocationHandler implements InvocationHandler {
        private final Class callbackListenerType;
        private final Messenger replyTo;
        private final CallbackListenerStub listenerStub;
        private final long resultHandlerId;

        public CallbackListenerInvocationHandler(Class callbackListenerType, Messenger replyTo,
                                                 CallbackListenerStub listenerStub, long resultHandlerId) {
            this.callbackListenerType = callbackListenerType;
            this.replyTo = replyTo;
            this.listenerStub = listenerStub;
            this.resultHandlerId = resultHandlerId;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("equals") && method.getParameterTypes().length == 1) {
                return handleEquals(args[0]);
            }
            if (method.getName().equals("hashCode") && method.getParameterTypes().length == 0) {
                return handleHashCode();
            }
            Invocation invocation = new Invocation(callbackListenerType, method.getName(), method.getParameterTypes(), args);
            Message message = Message.obtain();
            message.getData().putSerializable("callbackInvocation", invocation);
            message.getData().putLong("resultHandlerId", resultHandlerId);
            replyTo.send(message);
            return null;
        }

        private boolean handleEquals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || !obj.getClass().equals(this.getClass())) {
                return false;
            }
            CallbackListenerInvocationHandler handler = (CallbackListenerInvocationHandler) obj;
            return listenerStub.equals(handler.listenerStub);
        }

        private int handleHashCode() {
            return listenerStub.hashCode();
        }
    }

}
