package bratseth.maja.androidtest.service;

public class ResultHandlerProxy {

    private final ExceptionHandler handler;

    private ResultHandlerProxy(ExceptionHandler handler) {
        this.handler = handler;
    }

    public static ResultHandlerProxy createFor(ExceptionHandler exceptionHandler) {
        return new ResultHandlerProxy(exceptionHandler);
    }

    public void handle(InvocationResult invocationResult) throws Throwable {
        if (invocationResult.isException()) {
            if (invocationResult.getException() instanceof Exception && handler != null) {
                handler.exception((Exception) invocationResult.getException());
            } else {
                throw invocationResult.getException();
            }
        } else {
            if (!(handler instanceof ResultHandler) && invocationResult.getResult() != null) {
                throw new IllegalStateException("Expected no result but got " + invocationResult.getResult());
            }
            ((ResultHandler) handler).result(invocationResult.getResult());
        }
    }

}
