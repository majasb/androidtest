package bratseth.maja.androidtest.service;

import java.io.Serializable;

/**
 * @author Maja S Bratseth
 */
public class ResultHandlerStub implements ResultHandler, Serializable {

    private Object result;

    public void result(Object result) {
        this.result = result;
    }

    public void exception(Exception exception) {
    }

    public Object getResult() {
        return result;
    }

}
