package com.example.uiservice.gameclient;

import bratseth.maja.androidtest.service.CallbackEvent;

/**
 * @author Maja S Bratseth
 */
public abstract class TypedCallbackListenerBase<T extends CallbackEvent> implements TypedCallbackListener<T> {

    private final Class type;

    protected TypedCallbackListenerBase(Class type) {
        this.type = type;
    }

    public Class getType() {
        return this.type;
    }

}
