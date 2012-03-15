package bratseth.maja.msgtransport.transport;

import android.os.Message;
import bratseth.maja.androidtest.service.CallbackEvent;
import bratseth.maja.androidtest.service.Invocation;
import bratseth.maja.androidtest.service.InvocationResult;

/**
 * @author Maja S Bratseth
 */
public class TransportMessages {

    public static final int MSG_INVOKE = 0;
    public static final int MSG_REPLY = 1;
    public static final int MSG_REGISTER_LISTENER = 3;
    public static final int MSG_UNREGISTER_LISTENER = 4;
    public static final int MSG_CALLBACK = 5;

    public static Message createInvocation(Invocation invocation, long resultHandlerId) {
        Message msg = Message.obtain();
        msg.what = MSG_INVOKE;
        msg.getData().putSerializable("invocation", invocation);
        msg.getData().putLong("resultHandlerId", resultHandlerId);
        return msg;
    }

    public static Invocation extractInvocation(Message message) {
        return (Invocation) message.getData().getSerializable("invocation");
    }

    public static Message createInvocationReply(Message invocationMessage, InvocationResult result) {
        Message replyMessage = Message.obtain();
        replyMessage.what = MSG_REPLY;
        replyMessage.getData().putSerializable("result", result);
        replyMessage.getData().putLong("resultHandlerId", extractResultHandlerId(invocationMessage));
        return replyMessage;
    }

    private static long extractResultHandlerId(Message invocationMessage) {
        return invocationMessage.getData().getLong("resultHandlerId");
    }

    public static InvocationResult extractInvocationResult(Message msg) {
        return (InvocationResult) msg.getData().getSerializable("result");
    }

    public static Message createRegisterListener(Class eventType) {
        Message message = Message.obtain();
        message.what = MSG_REGISTER_LISTENER;
        message.getData().putSerializable("eventType", eventType);
        return message;
    }

    public static Message createUnregisterListener(Class eventType) {
        Message message = Message.obtain();
        message.what = MSG_UNREGISTER_LISTENER;
        message.getData().putSerializable("eventType", eventType);
        return message;
    }

    public static Class extractEventType(Message message) {
       return (Class) message.getData().getSerializable("eventType");
    }

    public static Message createCallback(CallbackEvent callback) {
        Message callbackMessage = Message.obtain();
        callbackMessage.what = MSG_CALLBACK;
        callbackMessage.getData().putSerializable("callbackMessage", callback);
        return callbackMessage;
    }

    public static CallbackEvent extractCallback(Message message) {
        return (CallbackEvent) message.getData().getSerializable("callbackMessage");
    }

}
