package main.java.com.example.kafkadisc;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import static java.lang.Thread.sleep;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        for(int i=1; i<=1; i++) {
            try (Socket socket = new Socket(HOST, PORT)) {
                System.out.println("Connected to server");

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                Produce message1 = new Produce("Hello ");
                Produce message2 = new Produce("from ");
                Produce message3 = new Produce("client 1");
                Produce message = new Produce(message1.data + message2.data + message3.data);
                byte[] bytes1 = Serializer.encode(message1);
                byte[] bytes2 = Serializer.encode(message2);
                byte[] bytes3 = Serializer.encode(message3);
                byte[] bytes = Serializer.encode(message);
                // Send 4-byte size header
                // Send packet data
                out.write(bytes1);
                out.flush();
                sleep(1000);
                out.write(bytes2);
                out.flush();
                sleep(1000);
                out.write(bytes3);
                out.flush();
                out.write(bytes);
                out.flush();
                System.out.println("Sent: " + message);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
