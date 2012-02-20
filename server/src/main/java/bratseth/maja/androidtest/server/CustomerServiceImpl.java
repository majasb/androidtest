package bratseth.maja.androidtest.server;

import java.util.*;

import bratseth.maja.androidtest.spi.Customer;
import bratseth.maja.androidtest.spi.CustomerId;
import bratseth.maja.androidtest.spi.CustomerService;

/**
 * @author Maja S Bratseth
 */
public class CustomerServiceImpl implements CustomerService {

    private final Map<CustomerId, Customer> customers = new HashMap<CustomerId, Customer>();

    public CustomerServiceImpl() {
        final CustomerId customerId = new CustomerId("1");
        customers.put(customerId, new Customer(customerId, "Customer " + customerId.getId()));
    }

    @Override
    public Customer getCustomer(CustomerId customerId) {
        if (!customers.containsKey(customerId)) {
            throw new IllegalArgumentException("No such customer: " + customerId);
        }
        return customers.get(customerId);
    }

}
