package bratseth.maja.msgtransport.transport;

import java.io.Serializable;

public class InvocationResult implements Serializable {
    
    private final Object result;

    private InvocationResult(Object result) {
        this.result = result;
    }

    public Object getResult() {
        return result;
    }

    public static InvocationResult exception(Throwable t) {
        return new InvocationResult(t);
    }
    
    public static InvocationResult normalResult(Object o) {
        return new InvocationResult(o);
    }

    public boolean isException() {
        return result instanceof Throwable;
    }

    public Throwable getException() {
        return (Throwable) result;
    }

}
