package bratseth.maja.androidtest.service;

import bratseth.maja.androidtest.service.TransportListener;

interface TransportService {

    byte[] invoke(in byte[] invocation);

    void register(in TransportListener listener);
}
