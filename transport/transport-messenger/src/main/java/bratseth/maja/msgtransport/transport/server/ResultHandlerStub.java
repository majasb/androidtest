package bratseth.maja.msgtransport.transport.server;

import java.io.Serializable;

import bratseth.maja.androidtest.service.ResultHandler;

public class ResultHandlerStub implements ResultHandler, Serializable {

    private boolean hasBeenSet = false;
    private Object result;
    private Exception exception;

    public void result(Object result) {
        if (hasBeenSet()) {
            throw new UnsupportedOperationException("Can only be used once");
        }
        this.result = result;
        hasBeenSet = true;
    }

    public void exception(Exception exception) {
        if (hasBeenSet()) {
            throw new UnsupportedOperationException("Can only be used once");
        }
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
