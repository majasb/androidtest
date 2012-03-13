package bratseth.maja.androidtest.service;

/**
 * @author Maja S Bratseth
 */
public interface ResultHandler<T> extends ExceptionHandler {

    void result(T result);

}
