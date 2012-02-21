package bratseth.maja.androidtest.spi;

import java.io.Serializable;

/**
 *
 */
public class CustomerEvent implements Serializable {

    private final CustomerId customerId;

    public CustomerEvent(CustomerId customerId) {
        this.customerId = customerId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

}
