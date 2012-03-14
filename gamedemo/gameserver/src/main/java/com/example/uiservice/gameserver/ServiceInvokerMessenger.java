package com.example.uiservice.gameserver;

import com.example.uiservice.spi.GameService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Messenger;
import bratseth.maja.msgtransport.transport.ServiceInvokerMessageHandler;

/**
 * @author Maja S Bratseth
 */
public class ServiceInvokerMessenger extends Service {

    private final ServiceInvokerMessageHandler messageHandler = new ServiceInvokerMessageHandler();
    private final Messenger messenger = new Messenger(messageHandler);

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public void onCreate() {
        final ServiceRegistry serviceRegistry = new ServiceRegistry();
        serviceRegistry.register(GameService.class, new GameServiceImpl(messageHandler));

        messageHandler.setServiceLocator(serviceRegistry);

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
