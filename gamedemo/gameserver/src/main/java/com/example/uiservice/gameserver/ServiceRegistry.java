package com.example.uiservice.gameserver;

import java.util.*;

import bratseth.maja.androidtest.service.ServiceLocator;

/**
 * @author Maja S Bratseth
 */
public class ServiceRegistry implements ServiceLocator {
    
    private final Map<Class, Object> services = new HashMap<Class, Object>();
    
    public <T> void register(Class<T> type, T service) {
        services.put(type, service);
    }

    @Override
    public <T> T locate(Class<T> type) {
        return type.cast(services.get(type));
    }

}
