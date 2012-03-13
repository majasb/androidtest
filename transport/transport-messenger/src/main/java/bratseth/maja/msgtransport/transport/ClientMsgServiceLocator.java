package bratseth.maja.msgtransport.transport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import bratseth.maja.androidtest.service.ExceptionHandler;
import bratseth.maja.androidtest.service.Invocation;
import bratseth.maja.androidtest.service.InvocationResult;
import bratseth.maja.androidtest.service.ResultHandler;
import bratseth.maja.androidtest.service.ResultHandlerProxy;
import bratseth.maja.androidtest.service.ServiceLocator;

public class ClientMsgServiceLocator implements ServiceLocator {

    private final Context context;

    // queued
    private final LinkedList<Invocation> commands = new LinkedList<Invocation>();
    private final Map<Invocation, List<ResultHandlerProxy>> resultHandlersForCommands =
        new HashMap<Invocation, List<ResultHandlerProxy>>();

    private final Map<Long, List<ResultHandlerProxy>> resultHandlers = new HashMap<Long, List<ResultHandlerProxy>>();
    private Messenger messenger;
    private final Messenger replyHandlerMessenger = new Messenger(new ReplyHandler());

    public ClientMsgServiceLocator(Context context) {
        this.context = context;
    }

    public <T> T locate(final Class<T> type) {
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                invokeService(type, method, args);
                return null;
            }
        };
        Object proxy = Proxy.newProxyInstance(type.getClassLoader(), new Class[] {type}, handler);
        return type.cast(proxy);
    }

    private class ReplyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            try {
                InvocationResult result = (InvocationResult) msg.getData().getSerializable("result");
                long resultHandlerId = msg.getData().getLong("resultHandlerId");
                List<ResultHandlerProxy> handlers = ClientMsgServiceLocator.this.resultHandlers.remove(resultHandlerId);
                Log.i(getClass().getSimpleName(), "Received result " + result);
                for (ResultHandlerProxy handler : handlers) {
                    handler.handle(result);
                }
            } catch (Throwable t) {
                defaultHandleException(t);
            }
        }
    }

    public void messengerConnected(Messenger messenger) {
        this.messenger = messenger;
        while (!commands.isEmpty()) {
            Invocation command = commands.pop();
            invokeRemoteService(command, resultHandlersForCommands.remove(command));
        }
    }

    public void messengerDisconnected() {
        messenger = null;
    }

    private void invokeService(Class type, Method method, Object[] parameters) {
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
        invokeRemoteService(invocation, resultHandlers);
    }

    private void invokeRemoteService(Invocation invocation, List<ResultHandlerProxy> resultHandlers) {
        if (messenger == null) {
            commands.addLast(invocation);
            resultHandlersForCommands.put(invocation, resultHandlers);
            return;
        }
        Message msg = Message.obtain(null, 1);
        Bundle data = new Bundle();
        data.putSerializable("invocation", invocation);
        msg.setData(data);

        long handlerId = System.identityHashCode(resultHandlers);
        data.putLong("resultHandlerId", handlerId);
        this.resultHandlers.put(handlerId, resultHandlers);

        msg.replyTo = replyHandlerMessenger;
        send(msg);
    }

    private void send(Message msg) {
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private void defaultHandleException(Throwable t) {
        final String text = "Error: " + t.getMessage();
        Log.e(getClass().getSimpleName(), "Handled exception. Message shown to user: " + text, t);
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

}
