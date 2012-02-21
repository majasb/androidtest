package bratseth.maja.androidtest.service.aidl;

import bratseth.maja.androidtest.service.aidl.AidlCustomer;
import bratseth.maja.androidtest.service.aidl.AidlCustomerId;

interface AidlCustomerService {

    AidlCustomer getCustomer(in AidlCustomerId customerId);

    long latencyTest(in AidlCustomerId customerId);

}
