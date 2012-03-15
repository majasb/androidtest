package bratseth.maja.androidtest.service;

public interface ResultHandler<T> extends ExceptionHandler {

    void result(T result);

}
