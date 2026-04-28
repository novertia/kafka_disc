package main.java.com.example.kafkadisc;

import java.io.*;
import java.nio.ByteBuffer;

public class Serializer {
    // Encode: Class to Byte Stream

    private static final int ONE_MB = 1024 * 1024;
    public static <T extends Serializable> byte[] encode(T obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {

            oos.writeObject(obj);
            byte[] data = bos.toByteArray();

            if (data.length > ONE_MB) {
                throw new IOException("Serialization failed: Object size (" + data.length +
                        " bytes) exceeds limit of " + ONE_MB + " bytes.");
            }
            return ByteBuffer.allocate(4 + data.length)
                    .putInt(data.length)
                    .put(data)
                    .array();
        }
    }

    // Decode: Byte array back to a specific Class type
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T decode(byte[] data)
            throws IOException, ClassNotFoundException {

        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (T) ois.readObject();
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

