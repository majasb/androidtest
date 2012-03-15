package bratseth.maja.msgtransport.transport.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.os.*;
import bratseth.maja.androidtest.service.*;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import bratseth.maja.androidtest.service.TypedCallbackListener;
import bratseth.maja.msgtransport.transport.TransportMessages;

public class MessengerClient implements ServiceLocator, EventBroker {

    private final Context context;

    // queued while no messenger
    private final LinkedList<Invocation> commandQueue = new LinkedList<Invocation>();
    private final Map<Invocation, ResultHandlerProxy> commandQueueHandlers = new HashMap<Invocation, ResultHandlerProxy>();
    private final LinkedList<Class> callbackEventTypeQueue = new LinkedList<Class>();

    private Messenger messenger;
    private final Messenger replyHandlerMessenger = new Messenger(new ReplyHandler());

    private final List<TypedCallbackListener> callbackListeners = new ArrayList<TypedCallbackListener>();

    private final Map<Long, ResultHandlerProxy> resultHandlers = new HashMap<Long, ResultHandlerProxy>();

    public MessengerClient(Context context) {
        this.context = context;
    }

    @Override
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

    @Override
    public void addListener(TypedCallbackListener callbackListener) {
        this.callbackListeners.add(callbackListener);
    }

    public void messengerConnected(Messenger messenger) {
        this.messenger = messenger;
        while (!commandQueue.isEmpty()) {
            Invocation command = commandQueue.pop();
            invokeRemoteService(command, commandQueueHandlers.remove(command));
        }
        while (!callbackEventTypeQueue.isEmpty()) {
            Class eventType = callbackEventTypeQueue.pop();
            registerRemoteListener(eventType);
        }
    }

    public void startEventListening() {
        for (TypedCallbackListener listener : callbackListeners) {
            registerRemoteListener(listener.getType());
        }
    }

    public void stopEventListening() {
        for (TypedCallbackListener listener : callbackListeners) {
            unregisterRemoteListener(listener.getType());
        }
    }

    public void messengerDisconnected() {
        messenger = null;
    }

    private void invokeService(Class type, Method method, Object[] parameters) {
        // Invoke service, but remove result/exception handlers, create proxy to handle asynchronous result
        final Class[] parameterTypes = method.getParameterTypes();
        final ResultHandlerProxy resultHandlerProxy;
        final Object[] adjustedParameters;
        if (parameterTypes.length > 0
            && ExceptionHandler.class.isAssignableFrom(parameterTypes[parameterTypes.length - 1])) {

            resultHandlerProxy = ResultHandlerProxy.createFor((ExceptionHandler) parameters[parameterTypes.length - 1]);
            adjustedParameters = new Object[parameters.length];
            System.arraycopy(parameters, 0, adjustedParameters, 0, parameterTypes.length - 1);
            adjustedParameters[parameters.length - 1] = null; // not needed on the server
        } else {
            resultHandlerProxy = ResultHandlerProxy.createFor(null);
            adjustedParameters = parameters;
        }
        final Invocation invocation = new Invocation(type, method.getName(), method.getParameterTypes(), adjustedParameters);
        invokeRemoteService(invocation, resultHandlerProxy);
    }

    private void invokeRemoteService(Invocation invocation, ResultHandlerProxy resultHandler) {
        if (messenger == null) {
            commandQueue.addLast(invocation);
            commandQueueHandlers.put(invocation, resultHandler);
            return;
        }
        long handlerId = System.identityHashCode(resultHandler);
        this.resultHandlers.put(handlerId, resultHandler);

        send(TransportMessages.createInvocation(invocation, handlerId));
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

    private void registerRemoteListener(Class callbackEventType) {
        if (messenger == null) {
            callbackEventTypeQueue.add(callbackEventType);
            return;
        }
        send(TransportMessages.createRegisterListener(callbackEventType));
    }

    private void unregisterRemoteListener(Class callbackEventType) {
        if (messenger != null) {
            send(TransportMessages.createUnregisterListener(callbackEventType));
        }
    }

    private void handleCallbackEvent(Message message) {
        CallbackEvent callbackEvent = TransportMessages.extractCallback(message);
        for (TypedCallbackListener listener : callbackListeners) {
            listener.handleEvent(callbackEvent);
        }
    }

    private void handleResult(Message msg) throws Throwable {
        InvocationResult result = TransportMessages.extractInvocationResult(msg);
        long resultHandlerId = TransportMessages.extractResultHandlerId(msg);
        ResultHandlerProxy handler = MessengerClient.this.resultHandlers.remove(resultHandlerId);
        handler.handle(result);
    }

    private String toLogString(Message msg) {
        return "" + msg.what + ": " + msg.getData();
    }

    private class ReplyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            try {
                if (msg.what == TransportMessages.MSG_REPLY) {
                    handleResult(msg);
                } else if (msg.what == TransportMessages.MSG_CALLBACK) {
                    handleCallbackEvent(msg);
                } else {
                    throw new IllegalArgumentException("Unknown message " + toLogString(msg));
                }
            } catch (Throwable t) {
                defaultHandleException(t);
            }
        }
    }

}
