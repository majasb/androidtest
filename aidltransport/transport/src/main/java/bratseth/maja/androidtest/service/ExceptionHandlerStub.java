package bratseth.maja.androidtest.service;

import java.io.Serializable;

/**
 * @author Maja S Bratseth
 */
public class ExceptionHandlerStub implements ExceptionHandler, Serializable {

    private Exception exception;

    @Override
    public void exception(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
