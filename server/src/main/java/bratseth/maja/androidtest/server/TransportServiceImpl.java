package bratseth.maja.androidtest.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.os.RemoteException;
import android.util.Log;
import bratseth.maja.androidtest.service.Invocation;
import bratseth.maja.androidtest.service.InvocationResult;
import bratseth.maja.androidtest.service.Serializer;
import bratseth.maja.androidtest.service.TransportService;

/**
 * @author Maja S Bratseth
 */
public class TransportServiceImpl extends TransportService.Stub {
    
    private final String tag = getClass().getSimpleName();

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

    private Object invokeService(Invocation invocation) throws Throwable {
        Class serviceType = Class.forName(invocation.getServiceType());
        Method method = findMethod(serviceType, invocation);
        Object service = findService(serviceType);
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

    private Method findMethod(Class serviceType, Invocation invocation) throws Exception {
        final String[] parameterClassNames = invocation.getParameterClasses();
        final int length = parameterClassNames.length;
        Class[] parameterTypes = new Class[length];
        for (int i = 0; i < length; i++) {
            parameterTypes[i] = Class.forName(parameterClassNames[i]);
        }
        return serviceType.getMethod(invocation.getMethodName(), parameterTypes);
    }

}
