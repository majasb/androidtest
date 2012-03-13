package bratseth.maja.androidtest.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Maja S Bratseth
 */
public class JavaSerializationSerializer implements Serializer {
    
    public byte[] writeObject(Object object) throws Exception {
        ByteArrayOutputStream sw = new ByteArrayOutputStream();
        ObjectOutputStream so = new ObjectOutputStream(sw);
        so.writeObject(object);
        so.flush();
        sw.close();
        return sw.toByteArray();
    }

    public Object readObject(byte[] bytes) throws Exception {
        ByteArrayInputStream istream = new ByteArrayInputStream(bytes);
        ObjectInputStream p = new ObjectInputStream(istream);
        Object deserialized = p.readObject();
        istream.close();
        return deserialized;
    }
    
}
