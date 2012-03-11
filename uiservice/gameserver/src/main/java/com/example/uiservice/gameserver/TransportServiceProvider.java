package com.example.uiservice.gameserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import bratseth.maja.androidtest.server.EventPublisher;
import bratseth.maja.androidtest.server.ServiceRegistry;
import bratseth.maja.androidtest.server.TransportServiceImpl;
import bratseth.maja.androidtest.service.JavaSerializationSerializer;

/**
 * @author Maja S Bratseth
 */
public class TransportServiceProvider extends Service {

    private TransportServiceImpl service;

    @Override
    public void onCreate() {
        service = new TransportServiceImpl();

        service.setSerializer(new JavaSerializationSerializer());

        final ServiceRegistry serviceRegistry = new ServiceRegistry();
        //serviceRegistry.register(GamS);

        service.setServiceLocator(serviceRegistry);

        EventPublisher publisher = EventPublisher.get();
        publisher.setTransport(service);
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return service;
    }

    @Override
    public void onDestroy() {
        EventPublisher publisher = EventPublisher.get();
        publisher.setTransport(null);
        super.onDestroy();
    }
}
