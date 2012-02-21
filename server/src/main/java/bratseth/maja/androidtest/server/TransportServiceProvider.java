package bratseth.maja.androidtest.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import bratseth.maja.androidtest.spi.CustomerService;

/**
 * @author Maja S Bratseth
 */
public class TransportServiceProvider extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        // this would be done in another service
        ServiceRegistry.get().register(CustomerService.class, new CustomerServiceImpl());

        return new TransportServiceImpl();
    }

}
