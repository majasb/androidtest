package bratseth.maja.androidtest.service;

/**
 * @author Maja S Bratseth
 */
public abstract class TypedCallbackListener<T extends CallbackEvent> implements CallbackListener {

    private final Class<T> type;

    protected TypedCallbackListener(Class<T> type) {
        this.type = type;
    }

    public Class getType() {
        return this.type;
    }

    public void handleEvent(Object callback) {
        handle(type.cast(callback));
    }

    protected abstract void handle(T cast);

}
