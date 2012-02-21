package bratseth.maja.androidtest.server;

import java.util.HashMap;
import java.util.Map;

import android.os.RemoteException;
import bratseth.maja.androidtest.service.aidl.AidlCustomer;
import bratseth.maja.androidtest.service.aidl.AidlCustomerId;
import bratseth.maja.androidtest.service.aidl.AidlCustomerService;
import bratseth.maja.androidtest.spi.Customer;
import bratseth.maja.androidtest.spi.CustomerId;

/**
 *
 */
public class AidlCustomerServiceImpl extends AidlCustomerService.Stub {

    private final Map<AidlCustomerId, AidlCustomer> customers = new HashMap<AidlCustomerId, AidlCustomer>();

    public AidlCustomerServiceImpl() {
        final AidlCustomerId customerId = new AidlCustomerId("1");
        customers.put(customerId, new AidlCustomer(customerId, "Customer " + customerId.getId()));
    }

    @Override
    public AidlCustomer getCustomer(AidlCustomerId customerId) {
        if (!customers.containsKey(customerId)) {
            throw new IllegalArgumentException("No such customer: " + customerId);
        }
        return customers.get(customerId);
    }

    @Override
    public long latencyTest(AidlCustomerId customerId) throws RemoteException {
        return System.nanoTime();
    }
}
