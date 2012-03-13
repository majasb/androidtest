package bratseth.maja.msgtransport.transport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.os.Handler;
import android.os.Message;
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

            Object result = invokeService(invocation);
            Message replyMessage = createReply(message, InvocationResult.normalResult(result));
            message.replyTo.send(replyMessage);
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

    private Message createReply(Message message, InvocationResult result) {
        Message replyMessage = Message.obtain();
        replyMessage.getData().putSerializable("result", result);
        replyMessage.getData().putLong("resultHandlerId", message.getData().getLong("resultHandlerId"));
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

    private Object invokeService(Invocation invocation) throws Throwable {
        Method method = findMethod(invocation);
        Object service = findService(invocation.getServiceType());
        if (service == null) {
            throw new IllegalArgumentException("No such service: " + invocation.getServiceType());
        }
        try {
            final Object[] parameters = invocation.getParameters();
            final Object result = method.invoke(service, parameters);
            // only supports one resulthandler for now
            if (parameters.length > 0 && parameters[parameters.length - 1] instanceof ResultHandlerStub) {
                ResultHandlerStub resultHandlerStub = (ResultHandlerStub) parameters[parameters.length - 1];
                return resultHandlerStub.getResult(); // TODO: remove exception field, and don't need to send from client
            }
            // TODO: if CallbackListener
            return result;
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private Object findService(Class serviceType) {
        return serviceLocator.locate(serviceType);
    }

    private Method findMethod(Invocation invocation) throws Exception {
        return invocation.getServiceType().getMethod(invocation.getMethodName(), invocation.getParameterClasses());
    }

}
