package com.example.uiservice.service;

import bratseth.maja.androidtest.service.ResultHandler;

public abstract class ResultHandlerBase<T> implements ResultHandler<T> {
    
    public abstract void result(T result);

    public void exception(Exception exception) {
        throw new RuntimeException(exception); // handled by service implementation
    }

}
