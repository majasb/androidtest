package bratseth.maja.androidtest.service;

/**
 * @author Maja S Bratseth
 */
public interface ServiceLocator {
    
    <T> T locate(Class<T> type);

    void addEventListener(ClientEventListener listener);
}
