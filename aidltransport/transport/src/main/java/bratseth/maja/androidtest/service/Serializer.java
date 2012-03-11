package bratseth.maja.androidtest.service;

/**
 * @author Maja S Bratseth
 */
public interface Serializer {
    
    Object readObject(byte[] data) throws Exception;

    byte[] writeObject(Object object) throws Exception;

}
