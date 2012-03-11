package bratseth.maja.androidtest.service;

import java.util.LinkedList;
import java.util.List;

public class ServiceEventListener extends TransportListener.Stub {

    private List<ClientEventListener> listeners = new LinkedList<ClientEventListener>();
    
    private Serializer serializer;

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public void notify(byte[] eventData) {
        try {
            Object event = serializer.readObject(eventData);
            notifyListeners(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void notifyListeners(Object event) {
        for (ClientEventListener listener : listeners) {
            listener.notify(event);
        }
    }

    public void add(ClientEventListener listener) {
        listeners.add(listener);
    }

}
