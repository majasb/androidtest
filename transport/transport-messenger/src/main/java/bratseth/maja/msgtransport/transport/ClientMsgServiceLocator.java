package bratseth.maja.msgtransport.transport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import bratseth.maja.androidtest.service.*;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import bratseth.maja.androidtest.service.ui.EventBroker;
import bratseth.maja.androidtest.service.CallbackListener;
import bratseth.maja.androidtest.service.TypedCallbackListener;

public class ClientMsgServiceLocator implements ServiceLocator, EventBroker {

    private final Context context;

    private final List<TypedCallbackListener> listeners = new ArrayList<TypedCallbackListener>();
    
    // queued
    private final LinkedList<Invocation> commands = new LinkedList<Invocation>();
    private final Map<Invocation, List<ResultHandlerProxy>> resultHandlersForCommands =
        new HashMap<Invocation, List<ResultHandlerProxy>>();
    private final LinkedList<Class> eventTypes = new LinkedList<Class>();

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
                if (msg.getData().containsKey("result")) {
                    InvocationResult result = (InvocationResult) msg.getData().getSerializable("result");
                    long resultHandlerId = msg.getData().getLong("resultHandlerId");
                    List<ResultHandlerProxy> handlers = ClientMsgServiceLocator.this.resultHandlers.remove(
                        resultHandlerId);
                    Log.i(getClass().getSimpleName(), "Received result " + result);
                    for (ResultHandlerProxy handler : handlers) {
                        handler.handle(result);
                    }
                } else if (msg.getData().containsKey("event")) {
                    handleEvent((CallbackEvent) msg.getData().getSerializable("event"));
                } else {
                    throw new IllegalArgumentException("Unknown message " + msg.getData());
                }
            } catch (Throwable t) {
                defaultHandleException(t);
            }
        }
    }

    private void handleEvent(CallbackEvent event) {
        for (CallbackListener listener : listeners) {
            listener.handleEvent(event);
        }
    }

    public void messengerConnected(Messenger messenger) {
        this.messenger = messenger;
        while (!commands.isEmpty()) {
            Invocation command = commands.pop();
            invokeRemoteService(command, resultHandlersForCommands.remove(command));
        }
        while (!eventTypes.isEmpty()) {
            Class eventType = eventTypes.pop();
            registerRemoteListener(eventType);
        }
    }

    public void stopEventListening() {
        for (TypedCallbackListener listener : listeners) {
            deregisterRemoteListener(listener.getType());
        }
        listeners.clear();
    }

    public void messengerDisconnected() {
        messenger = null;
    }

    private void invokeService(Class type, Method method, Object[] parameters) {
        List<ResultHandlerProxy> resultHandlers = new ArrayList<ResultHandlerProxy>();
        Object[] adjustedParameters = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            final Object parameter = parameters[i];
            final Object placeholderParameter;
            if (parameter instanceof ResultHandler) {
                final ResultHandlerProxy proxy = ResultHandlerProxy.createFor((ResultHandler) parameter);
                resultHandlers.add(proxy);
                placeholderParameter = proxy.createStub();
            } else if (parameter instanceof ExceptionHandler) {
                final ResultHandlerProxy proxy = ResultHandlerProxy.createFor((ExceptionHandler) parameter);
                resultHandlers.add(proxy);
                placeholderParameter = proxy.createStub();
            }
            else {
                placeholderParameter = parameter;
            }
            adjustedParameters[i] = placeholderParameter;
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

        send(msg);
    }

    private void send(Message msg) {
        try {
            msg.replyTo = replyHandlerMessenger;
            messenger.send(msg);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private void defaultHandleException(Throwable t) {
        final String text = "Error: " + t.getMessage();
        Log.e(getClass().getSimpleName(), "Handled exception. Message shown to user: " + text, t);
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public void addListener(TypedCallbackListener callbackListener) {
        this.listeners.add(callbackListener);
        registerRemoteListener(callbackListener.getType());
    }

    private void registerRemoteListener(Class type) {
        if (messenger == null) {
            eventTypes.add(type);
            return;
        }
        Message message = Message.obtain();
        message.getData().putString("registerListener", null);
        message.getData().putSerializable("eventType", type);
        send(message);
    }

    private void deregisterRemoteListener(Class type) {
        Message message = Message.obtain();
        message.getData().putString("deregisterListener", null);
        message.getData().putSerializable("eventType", type);
        send(message);
    }

}
