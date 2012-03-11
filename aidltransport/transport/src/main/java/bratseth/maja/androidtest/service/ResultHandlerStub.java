package bratseth.maja.androidtest.service;

import java.io.Serializable;

/**
 * @author Maja S Bratseth
 */
public class ResultHandlerStub implements ResultHandler, Serializable {

    private Object result;
    private Exception exception;

    @Override
    public void result(Object result) {
        this.result = result;
    }

    @Override
    public void exception(Exception exception) {
        this.exception = exception;
    }

    public Object getResult() {
        return result;
    }

    public Exception getException() {
        return exception;
    }
}