package com.example.uiservice.ui;

/**
 * @author Maja S Bratseth
 */
public abstract class BackgroundExecutable<R> {
    
    public abstract R runInBackground();
    
    public abstract void onResult(R result);
    
    public void onException(Exception e) {
        throw new RuntimeException(e); // handled by executor
    }
    
}
