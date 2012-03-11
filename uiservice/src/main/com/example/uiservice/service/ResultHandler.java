package com.example.uiservice.service;

public abstract class ResultHandler<T> {
    
    public abstract void result(T result);
    
    public void exception(Exception exception) {
        throw new RuntimeException(exception); // handled by service implementation
    }

}
