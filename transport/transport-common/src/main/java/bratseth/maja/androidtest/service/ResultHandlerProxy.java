package bratseth.maja.androidtest.service;

/**
 * @author Maja S Bratseth
 */
public class ResultHandlerProxy {

    private final ResultHandler resultHandler;
    private final ExceptionHandler exceptionHandler;

    private ResultHandlerProxy(ResultHandler resultHandler, ExceptionHandler exceptionHandler) {
        this.resultHandler = resultHandler;
        this.exceptionHandler = exceptionHandler;
    }

    public static ResultHandlerProxy createFor(ResultHandler resultHandler) {
        return new ResultHandlerProxy(resultHandler, resultHandler);
    }

    public static ResultHandlerProxy createFor(ExceptionHandler exceptionHandler) {
        return new ResultHandlerProxy(null, exceptionHandler);
    }

    public void handle(InvocationResult invocationResult) throws Throwable {
        if (invocationResult.isException()) {
            if (invocationResult.getException() instanceof Exception) {
                exceptionHandler.exception((Exception) invocationResult.getException());
            } else {
                throw invocationResult.getException();
            }
        } else {
            if (resultHandler == null && invocationResult.getResult() != null) {
                throw new IllegalStateException("Expected no result but got " + invocationResult.getResult());
            }
            resultHandler.result(invocationResult.getResult());
        }
    }

}
