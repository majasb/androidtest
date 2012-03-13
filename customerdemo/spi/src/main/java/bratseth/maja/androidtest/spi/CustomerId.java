package bratseth.maja.androidtest.spi;

import java.io.Serializable;

/**
 * @author Maja S Bratseth
 */
public class CustomerId implements Serializable {
    
    private final String id;

    public CustomerId(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CustomerId that = (CustomerId) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + System.identityHashCode(this) + "{" + id + "}";
    }

}
