package bratseth.maja.androidtest.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Maja S Bratseth
 */
public class ClientServiceLocator implements ServiceLocator {

    private final Context context;
    private final TransportService transportService;
    private final ServiceEventListener serviceListener;
    private final Serializer serializer;

    public ClientServiceLocator(Context context, TransportService transportService, Serializer serializer) {
        this.context = context;
        this.transportService = transportService;
        this.serializer = serializer;
        serviceListener = new ServiceEventListener();
        serviceListener.setSerializer(serializer);
        registerServiceListener();
    }

    @Override
    public <T> T locate(final Class<T> type) {
        InvocationHandler invocationHandler = new InvocationHandler() {
            public Object invoke(Object o, Method method, Object[] parameters) throws Throwable {
                return doInvoke(method, parameters, type);
            }
        };
        final Object proxy = Proxy.newProxyInstance(type.getClassLoader(), new Class[] {type}, invocationHandler);
        return type.cast(proxy);
    }

    private Object doInvoke(Method method, Object[] parameters, Class type) throws Throwable {
        InvocationResult result = invokeRemoteService(method, parameters, type);
        if (result.isException()) {
            final Throwable exception = result.getException();
            throw new RuntimeException(exception.getMessage(), exception);
        }
        return result.getResult();
    }

    private InvocationResult invokeRemoteService(Method method, Object[] parameters, Class type) throws Throwable {
        List<ResultHandlerProxy> resultHandlers = new ArrayList<ResultHandlerProxy>();
        Object[] adjustedParameters = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            final Object parameter = parameters[i];
            final Object result;
            if (parameter instanceof ResultHandler) {
                final ResultHandlerProxy proxy = ResultHandlerProxy.createFor((ResultHandler) parameter);
                resultHandlers.add(proxy);
                result = proxy.createStub();
            } else if (parameter instanceof ExceptionHandler) {
                final ResultHandlerProxy proxy = ResultHandlerProxy.createFor((ExceptionHandler) parameter);
                resultHandlers.add(proxy);
                result = proxy.createStub();
            } else {
                result = parameter;
            }
            adjustedParameters[i] = result;
        }
        final Invocation invocation = new Invocation(type, method.getName(), method.getParameterTypes(),
                                                     adjustedParameters);
        byte[] invocationBytes = serializer.writeObject(invocation);
        final byte[] resultBytes = doInvoke(invocationBytes);
        final InvocationResult invocationResult = (InvocationResult) serializer.readObject(resultBytes);
        Log.i(getClass().getSimpleName(), "Result handlers: " + resultHandlers);
        if (!resultHandlers.isEmpty()) {
            try {
                for (ResultHandlerProxy resultHandler : resultHandlers) {
                    resultHandler.handle(invocationResult);
                }
                return InvocationResult.normalResult(null);
            } catch (Throwable t) {
                defaultHandleException(t);
                return InvocationResult.normalResult(null); // because it's handled
            }
        }
        return invocationResult;
    }

    private byte[] doInvoke(byte[] invocationBytes) throws RemoteException {
        try {
            return transportService.invoke(invocationBytes);
        } catch (Throwable e) {
            Log.i(getClass().getSimpleName(), "Caught exception from remote service!");
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    private void defaultHandleException(Throwable t) {
        final String text = "Error: " + t.getMessage();
        Log.e(getClass().getSimpleName(), "Handled exception. Message shown to user: " + text, t);
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void addEventListener(ClientEventListener listener) {
        serviceListener.add(listener);
    }

    private void registerServiceListener() {
        try {
            transportService.register(serviceListener);
        } catch (RemoteException e) {
            throw new RuntimeException("Unable to register event listener", e);
        }
    }

}
