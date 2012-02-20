package bratseth.maja.androidtest.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Maja S Bratseth
 */
public class ClientServiceLocator implements ServiceLocator {

    private final TransportService transportService;

    public ClientServiceLocator(TransportService transportService) {
        this.transportService = transportService;
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

    private Invocation toInvocation(Method method, Object[] parameters, Class type) {
        Class[] parameterTypes = method.getParameterTypes();
        String[] parameterClassNames = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterClassNames[i] = parameterTypes[i].getName();
        }
        return new Invocation(type.getName(), method.getName(), parameterClassNames, parameters);
    }

    private InvocationResult invokeRemoteService(Invocation invocation) throws Exception {
        Serializer serializer = Serializer.get();
        byte[] invocationBytes = serializer.writeObject(invocation);
        final byte[] resultBytes = transportService.invoke(invocationBytes);
        return (InvocationResult) serializer.readObject(resultBytes);
    }

}
