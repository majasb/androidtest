package bratseth.maja.androidtest.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import bratseth.maja.androidtest.service.JavaSerializationSerializer;
import bratseth.maja.androidtest.spi.CustomerService;

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
        serviceRegistry.register(CustomerService.class, new CustomerServiceImpl());

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
