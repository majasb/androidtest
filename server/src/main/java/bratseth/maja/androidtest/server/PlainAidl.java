package bratseth.maja.androidtest.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import bratseth.maja.androidtest.spi.CustomerService;

/**
 *
 */
public class PlainAidl extends Service {
    private AidlCustomerServiceImpl service;

    @Override
    public void onCreate() {
        service = new AidlCustomerServiceImpl();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return service;
    }
}
