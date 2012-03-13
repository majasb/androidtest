package bratseth.maja.androidtest.service;

public interface ServiceLocator {

    <T> T locate(Class<T> type);

}
