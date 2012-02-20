package bratseth.maja.androidtest.spi;

import java.io.Serializable;

/**
 * @author Maja S Bratseth
 */
public class Customer implements Serializable {
    
    private final CustomerId id;
    private final String name;

    public Customer(CustomerId id, String name) {
        this.id = id;
        this.name = name;
    }

    public CustomerId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
