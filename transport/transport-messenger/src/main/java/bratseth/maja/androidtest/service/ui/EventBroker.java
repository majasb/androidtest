package bratseth.maja.androidtest.service.ui;

import bratseth.maja.androidtest.service.TypedCallbackListener;

/**
 * @author Maja S Bratseth
 */
public interface EventBroker {

    void addListener(TypedCallbackListener callbackListener);

}
