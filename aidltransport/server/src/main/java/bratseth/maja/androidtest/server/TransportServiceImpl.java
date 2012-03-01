package bratseth.maja.androidtest.server;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import android.os.RemoteException;
import android.util.Log;
import bratseth.maja.androidtest.service.Invocation;
import bratseth.maja.androidtest.service.InvocationResult;
import bratseth.maja.androidtest.service.Serializer;
import bratseth.maja.androidtest.service.TransportListener;
import bratseth.maja.androidtest.service.TransportService;

/**
 * @author Maja S Bratseth
 */
public class TransportServiceImpl extends TransportService.Stub {
    
    private final String tag = getClass().getSimpleName();
    private List<TransportListener> listeners = new LinkedList<TransportListener>();

    @Override
    public byte[] invoke(byte[] invocation) throws RemoteException {
        final Serializer serializer = Serializer.get();
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
            return method.invoke(service, invocation.getParameters());
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private Object findService(Class serviceType) {
        return ServiceRegistry.get().locate(serviceType);
    }

    private Method findMethod(Invocation invocation) throws Exception {
        return invocation.getServiceType().getMethod(invocation.getMethodName(), invocation.getParameterClasses());
    }

    public void publish(Serializable e) throws Exception {
        final Serializer serializer = Serializer.get();
        byte[] bytes = serializer.writeObject(e);
        for (TransportListener listener : listeners) {
            listener.notify(bytes);
        }
    }
}
