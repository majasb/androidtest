package bratseth.maja.androidtest.service;

/**
 * @author Maja S Bratseth
 */
public class ResultHandlerProxy {

    private final ExceptionHandler exceptionHandler;

    private ResultHandlerProxy(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public static ResultHandlerProxy createFor(ExceptionHandler exceptionHandler) {
        return new ResultHandlerProxy(exceptionHandler);
    }

    public void handle(InvocationResult invocationResult) throws Throwable {
        if (invocationResult.isException()) {
            if (invocationResult.getException() instanceof Exception) {
                exceptionHandler.exception((Exception) invocationResult.getException());
            } else {
                throw invocationResult.getException();
            }
        } else {
            if (!(exceptionHandler instanceof ResultHandler) && invocationResult.getResult() != null) {
                throw new IllegalStateException("Expected no result but got " + invocationResult.getResult());
            }
            ((ResultHandler) exceptionHandler).result(invocationResult.getResult());
        }
    }

}
