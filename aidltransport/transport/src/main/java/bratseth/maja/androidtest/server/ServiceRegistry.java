package bratseth.maja.androidtest.server;

import java.util.*;

import bratseth.maja.androidtest.service.ClientEventListener;
import bratseth.maja.androidtest.service.ServiceLocatorWithCallback;

/**
 * @author Maja S Bratseth
 */
public class ServiceRegistry implements ServiceLocatorWithCallback {

    private final Map<Class, Object> services = new HashMap<Class, Object>();

    @Override
    public <T> T locate(Class<T> type) {
        return type.cast(services.get(type));
    }

    @Override
    public void addEventListener(ClientEventListener listener) {

    }

    public <T> void register(Class<T> type, T service) {
        services.put(type, service);
    }
    
}
