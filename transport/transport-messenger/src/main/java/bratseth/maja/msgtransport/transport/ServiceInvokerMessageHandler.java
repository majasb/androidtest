package bratseth.maja.msgtransport.transport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import bratseth.maja.androidtest.service.Invocation;
import bratseth.maja.androidtest.service.InvocationResult;
import bratseth.maja.androidtest.service.ResultHandlerStub;
import bratseth.maja.androidtest.service.Serializer;
import bratseth.maja.androidtest.service.ServiceLocator;

public class ServiceInvokerMessageHandler extends Handler {

    private final String tag = getClass().getSimpleName();

    private Serializer serializer;
    private ServiceLocator serviceLocator;

    @Override
    public void handleMessage(Message message) {
        try {
            Invocation invocation = (Invocation) message.getData().getSerializable("invocation");
            log("Got message: " + invocation);

            Object result = invokeService(message, invocation);
            if (isRegisterCallbackListenerMessage(message, invocation)) {
                if (result != null) {
                    throw new IllegalStateException("Excpected no result for callback register method, got " + result);
                }
                // callback will come later
            } else {
                Message replyMessage = createReply(message, InvocationResult.normalResult(result));
                message.replyTo.send(replyMessage);
            }
        } catch (Throwable e) {
            try {
                Log.e(tag, "Caught exception. Passing on to client", e);
                Message replyMessage = createReply(message, InvocationResult.exception(e));
                message.replyTo.send(replyMessage);
            } catch (Exception e2) {
                final String msg = "Caught exception and could not send it as reply. Time: " + System.currentTimeMillis();
                Log.e(tag, msg, e);
            }
        }
    }

    private boolean isRegisterCallbackListenerMessage(Message message, Invocation invocation) {
        // TODO: Declare this excplicitly
        return invocation.getParameters().length == 1 && invocation.getParameters()[0] instanceof CallbackListenerStub;
    }

    private Message createReply(Message invocationMessage, InvocationResult result) {
        Message replyMessage = Message.obtain();
        replyMessage.getData().putSerializable("result", result);
        replyMessage.getData().putLong("resultHandlerId", invocationMessage.getData().getLong("resultHandlerId"));
        return replyMessage;
    }

    private void log(String msg) {
        Log.i(getClass().getSimpleName(), msg);
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
