package com.example.uiservice.gameclient;

import bratseth.maja.androidtest.service.CallbackEvent;

/**
 * @author Maja S Bratseth
 */
public interface TypedCallbackListener<T extends CallbackEvent> {

    void handle(T callback);

    Class getType();

}
