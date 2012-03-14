package bratseth.maja.androidtest.service;

import java.lang.reflect.Method;

/**
 * @author Maja S Bratseth
 */
public class ResultHandlerProxy {

    private final ResultHandler resultHandler;
    private final ExceptionHandler exceptionHandler;
    private final CallbackListener callbackListener;

    private ResultHandlerProxy(ResultHandler resultHandler, ExceptionHandler exceptionHandler, CallbackListener callbackListener) {
        this.resultHandler = resultHandler;
        this.exceptionHandler = exceptionHandler;
        this.callbackListener = callbackListener;
    }

    public static ResultHandlerProxy createFor(ResultHandler resultHandler) {
        return new ResultHandlerProxy(resultHandler, resultHandler, null);
    }

    public static ResultHandlerProxy createFor(ExceptionHandler exceptionHandler) {
        return new ResultHandlerProxy(null, exceptionHandler, null);
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

    public Object createStub() {
        if (resultHandler != null) {
            return new ResultHandlerStub();
        } if (callbackListener != null) {
            return new CallbackListenerStub(System.identityHashCode(callbackListener));
        } else {
            return new ExceptionHandlerStub();
        }
    }

    public void handle(Invocation invocation) throws Throwable {
        Method callbackMethod =
            invocation.getServiceType().getDeclaredMethod(invocation.getMethodName(), invocation.getParameterClasses());
        callbackMethod.invoke(callbackListener, invocation.getParameters());
    }

    public static ResultHandlerProxy createFor(CallbackListener parameter) {
        return new ResultHandlerProxy(null, null, parameter);
    }
    
}
