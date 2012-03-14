package bratseth.maja.androidtest.service;

import java.io.Serializable;

public class CallbackListenerStub implements Serializable {

    private final long objectIdentifier;

    public CallbackListenerStub(long objectIdentifier) {
        this.objectIdentifier = objectIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CallbackListenerStub that = (CallbackListenerStub) o;

        return objectIdentifier == that.objectIdentifier;
    }

    @Override
    public int hashCode() {
        return (int) (objectIdentifier ^ (objectIdentifier >>> 32));
    }

}
