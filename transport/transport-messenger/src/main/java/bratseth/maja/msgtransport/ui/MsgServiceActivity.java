package bratseth.maja.msgtransport.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import bratseth.maja.androidtest.service.EventBroker;
import bratseth.maja.androidtest.service.ServiceLocator;
import bratseth.maja.msgtransport.transport.client.MessengerClient;

public abstract class MsgServiceActivity extends Activity {

    private MessengerClient messengerClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final MessengerClient messengerClient = new MessengerClient(getApplicationContext());

        ServiceConnection messengerConnection = createMessengerConnection(messengerClient);
        bindMessenger(messengerConnection);

        this.messengerClient = messengerClient;
    }

    @Override
    protected void onResume() {
        super.onResume();
        messengerClient.startEventListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        messengerClient.stopEventListening();
    }

    private ServiceConnection createMessengerConnection(final MessengerClient messengerClient) {
        return new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Messenger messenger = new Messenger(binder);
                messengerClient.messengerConnected(messenger);
            }
            public void onServiceDisconnected(ComponentName name) {
                messengerClient.messengerDisconnected();
            }
        };
    }

    private void bindMessenger(ServiceConnection serviceConnection) {
        boolean ok = bindService(new Intent("bratseth.maja.androidtest.msgtransport.ServiceInvokerMessenger"),
                                 serviceConnection,
                                 Context.BIND_AUTO_CREATE);
        if (!ok) {
            throw new RuntimeException("Could not bind");
        }
    }

    protected ServiceLocator getServiceLocator() {
        return messengerClient;
    }

    protected EventBroker getEventBroker() {
        return messengerClient;
    }
    
}
