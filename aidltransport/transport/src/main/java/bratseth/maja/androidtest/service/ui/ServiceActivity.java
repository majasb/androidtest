package bratseth.maja.androidtest.service.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import bratseth.maja.androidtest.service.ClientServiceLocator;
import bratseth.maja.androidtest.service.JavaSerializationSerializer;
import bratseth.maja.androidtest.service.ServiceLocator;
import bratseth.maja.androidtest.service.TransportService;

/**
 * @author Maja S Bratseth
 */
public abstract class ServiceActivity extends Activity {

    private ServiceLocator serviceLocator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ServiceConnection connection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                serviceLocator = new ClientServiceLocator(getApplicationContext(),
                                                          TransportService.Stub.asInterface(service),
                                                          new JavaSerializationSerializer());
            }
            public void onServiceDisconnected(ComponentName className) {
                finish();
            }
        };
        bindService(new Intent(TransportService.class.getName()), connection, Context.BIND_AUTO_CREATE);
    }

    protected ServiceLocator getServiceLocator() {
        if (serviceLocator == null) {
            throw new IllegalStateException("No services yet");
        }
        return serviceLocator;
    }
    
}
