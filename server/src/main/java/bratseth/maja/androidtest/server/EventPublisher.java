package bratseth.maja.androidtest.server;

import java.io.Serializable;

import android.util.Log;
import bratseth.maja.androidtest.service.TransportService;

/**
 *
 */
public class EventPublisher {

    private static final EventPublisher instance = new EventPublisher();
    private TransportServiceImpl service;

    public static EventPublisher get() {
        return instance;
    }

    public void setTransport(TransportServiceImpl service) {
        this.service = service;
    }

    public void publish(Serializable e) {
        try {
            if (service != null)
                service.publish(e);
        } catch (Exception ex) {
            Log.e("cisco", "Failed to publish event", ex);
        }
    }
}
