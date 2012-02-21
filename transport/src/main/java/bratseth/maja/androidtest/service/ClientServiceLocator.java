package bratseth.maja.androidtest.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.os.RemoteException;

/**
 * @author Maja S Bratseth
 */
public class ClientServiceLocator implements ServiceLocator {

    private final TransportService transportService;
    private final ServiceEventListener serviceListener;

    public ClientServiceLocator(TransportService transportService) {
        this.transportService = transportService;
        serviceListener = new ServiceEventListener();
        registerServiceListener();
    }

    @Override
    public <T> T locate(final Class<T> type) {
        InvocationHandler invocationHandler = new InvocationHandler() {
            public Object invoke(Object o, Method method, Object[] parameters) throws Throwable {
                final Invocation invocation = toInvocation(method, parameters, type);
                InvocationResult result = invokeRemoteService(invocation);
                if (result.isException()) {
                    final Throwable exception = result.getException();
                    throw new RuntimeException(exception.getMessage(), exception);
                }
                return result.getResult();
            }
        };
        final Object proxy = Proxy.newProxyInstance(type.getClassLoader(), new Class[] {type}, invocationHandler);
        return type.cast(proxy);
    }

    @Override
    public void addEventListener(ClientEventListener listener) {
        serviceListener.add(listener);
    }

    private Invocation toInvocation(Method method, Object[] parameters, Class type) {
        return new Invocation(type, method.getName(), method.getParameterTypes(), parameters);
    }

    private InvocationResult invokeRemoteService(Invocation invocation) throws Exception {
        Serializer serializer = Serializer.get();
        byte[] invocationBytes = serializer.writeObject(invocation);
        final byte[] resultBytes = transportService.invoke(invocationBytes);
        return (InvocationResult) serializer.readObject(resultBytes);
    }
    
    private void registerServiceListener() {
        try {
            transportService.register(serviceListener);
        } catch (RemoteException e) {
            throw new RuntimeException("Unable to register event listener", e);
        }
    }

}
