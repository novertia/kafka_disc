package main.java.com.example.kafkadisc.Storage;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

public class LogStorageTest {
    public static void main(String[] args) {
        LogStorage storage = new LogStorage();
        String testPayload = "Hello mmap!";
        String filePath = "/tmp/kafka_disc.log";

        // Clean up before test
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }

        try {
            System.out.println("Appending: " + testPayload);
            storage.append(testPayload);

            // Read back to verify
            try (DataInputStream dis = new DataInputStream(new FileInputStream(filePath))) {
                int size = dis.readInt();
                byte[] data = new byte[size];
                dis.readFully(data);
                String result = new String(data);
                
                System.out.println("Read back size: " + size);
                System.out.println("Read back data: " + result);

                if (testPayload.equals(result) && size == testPayload.getBytes().length) {
                    System.out.println("TEST PASSED");
                } else {
                    System.out.println("TEST FAILED");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
