package bratseth.maja.androidtest.server;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import android.os.RemoteException;
import android.util.Log;
import bratseth.maja.androidtest.service.*;

/**
 * @author Maja S Bratseth
 */
public class TransportServiceImpl extends TransportService.Stub {
    
    private final String tag = getClass().getSimpleName();
    private Serializer serializer;
    private final List<TransportListener> listeners = new LinkedList<TransportListener>();
    private ServiceLocator serviceLocator;

    public void setServiceLocator(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public byte[] invoke(byte[] invocation) throws RemoteException {
        try {
            Invocation unmarshalledInvocation = (Invocation) serializer.readObject(invocation);
            Object result = invokeService(unmarshalledInvocation);
            return serializer.writeObject(InvocationResult.normalResult(result));
        } catch (Throwable e) {
            try {
                Log.e(tag, "Caught exception. Passing on to client", e);
                return serializer.writeObject(InvocationResult.exception(e));
            } catch (Exception e2) {
                final String msg = "Caught exception and could not marshal it. Time: " + System.currentTimeMillis();
                Log.e(tag, msg, e);
                return msg.getBytes();
            }
        }
    }

    @Override
    public void register(TransportListener listener) throws RemoteException {
        listeners.add(listener);
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

    public void publish(Serializable e) throws Exception {
        byte[] bytes = serializer.writeObject(e);
        for (TransportListener listener : listeners) {
            listener.notify(bytes);
        }
    }

}
