package bratseth.maja.androidtest.service.ui;

import com.example.uiservice.gameclient.TypedCallbackListener;

/**
 * @author Maja S Bratseth
 */
public interface EventBroker {

    void addListener(TypedCallbackListener callbackListener);

}
