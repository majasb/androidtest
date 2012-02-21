package bratseth.maja.androidtest.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import bratseth.maja.androidtest.spi.CustomerService;

/**
 * @author Maja S Bratseth
 */
public class TransportServiceProvider extends Service {

    private TransportServiceImpl service;

    @Override
    public void onCreate() {
        service = new TransportServiceImpl();
        EventPublisher publisher = EventPublisher.get();
        publisher.setTransport(service);
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // this would be done in another service
        ServiceRegistry.get().register(CustomerService.class, new CustomerServiceImpl());
        return service;
    }

    @Override
    public void onDestroy() {
        EventPublisher publisher = EventPublisher.get();
        publisher.setTransport(null);
        super.onDestroy();
    }
}
