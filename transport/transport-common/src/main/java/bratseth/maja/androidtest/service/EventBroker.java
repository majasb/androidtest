package bratseth.maja.androidtest.service;

/**
 * @author Maja S Bratseth
 */
public interface EventBroker {

    void addListener(TypedCallbackListener callbackListener);

}
