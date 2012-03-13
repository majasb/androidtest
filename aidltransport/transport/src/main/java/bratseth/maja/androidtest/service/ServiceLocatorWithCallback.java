package bratseth.maja.androidtest.service;

/**
 * @author Maja S Bratseth
 */
public interface ServiceLocatorWithCallback extends ServiceLocator {

    void addEventListener(ClientEventListener listener);

}
