package bratseth.maja.androidtest.service.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import bratseth.maja.androidtest.service.ServiceLocator;
import bratseth.maja.msgtransport.transport.ClientMsgServiceLocator;

/**
 * @author Maja S Bratseth
 */
public abstract class MsgServiceActivity extends Activity {

    private ServiceLocator serviceLocator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ClientMsgServiceLocator msgServiceLocator = new ClientMsgServiceLocator(getApplicationContext());
        ServiceConnection messengerConnection = createMessengerConnection(msgServiceLocator);
        this.serviceLocator = msgServiceLocator;

        bindMessenger(messengerConnection);
    }

    private ServiceConnection createMessengerConnection(final ClientMsgServiceLocator serviceExecutor) {
        return new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Messenger messenger = new Messenger(binder);
                serviceExecutor.messengerConnected(messenger);
            }
            public void onServiceDisconnected(ComponentName name) {
                serviceExecutor.messengerDisconnected();
            }
        };
    }

    private void bindMessenger(ServiceConnection serviceConnection) {
        boolean ok = bindService(new Intent("com.cisco.oberon.messenger.serviceInvocation"),
                                 serviceConnection,
                                 Context.BIND_AUTO_CREATE);
        if (!ok) {
            throw new RuntimeException("Could not bind");
        }
    }


    protected ServiceLocator getServiceLocator() {
        if (serviceLocator == null) {
            throw new IllegalStateException("No services yet");
        }
        return serviceLocator;
    }
    
}
