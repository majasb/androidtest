package bratseth.maja.msgtransport.transport.server;

import java.io.Serializable;

import bratseth.maja.androidtest.service.ResultHandler;

/**
 * @author Maja S Bratseth
 */
public class ResultHandlerStub implements ResultHandler, Serializable {

    private boolean hasBeenSet = false;
    private Object result;
    private Exception exception;

    public void result(Object result) {
        this.result = result;
        hasBeenSet = true;
    }

    public void exception(Exception exception) {
        this.exception = exception;
        hasBeenSet = false;
    }

    public boolean hasBeenSet() {
        return hasBeenSet;
    }

    public Object getResult() {
        return result;
    }

    public Exception getException() {
        return exception;
    }

    public boolean isException() {
        return exception != null;
    }

}
