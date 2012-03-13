package bratseth.maja.androidtest.spi;

/**
 * @author Maja S Bratseth
 */
public interface CustomerService {
    
    Customer getCustomer(CustomerId customerId);

    void publishSomething();
}
