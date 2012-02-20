package bratseth.maja.androidtest.server;

import java.util.*;

import bratseth.maja.androidtest.service.ServiceLocator;

/**
 * @author Maja S Bratseth
 */
public class ServiceRegistry implements ServiceLocator {

    private static final ServiceRegistry instance = new ServiceRegistry();

    public static ServiceRegistry get() {
        return instance;
    }
    
    private final Map<Class, Object> services = new HashMap<Class, Object>();

    @Override
    public <T> T locate(Class<T> type) {
        return type.cast(services.get(type));
    }

    public <T> void register(Class<T> type, T service) {
        services.put(type, service);
    }
    
}
