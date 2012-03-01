package bratseth.maja.androidtest.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.os.RemoteException;

public class ServiceEventListener extends TransportListener.Stub {

    private List<ClientEventListener> listeners = new LinkedList<ClientEventListener>();

    @Override
    public void notify(byte[] event) {
        try {
            Object e = Serializer.get().readObject(event);
            notifyListeners(e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void notifyListeners(Object e) {
        for (ClientEventListener listener : listeners) {
            listener.notify(e);
        }
    }

    public void add(ClientEventListener listener) {
        listeners.add(listener);
    }
}
