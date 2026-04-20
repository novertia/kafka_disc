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
                String message1 = "Hello ";
                String message2 = "from ";
                String message3 = "Client!";
                String message = message1 + message2 + message3;
                byte[] bytes1 = message1.getBytes(StandardCharsets.UTF_8);
                byte[] bytes2 = message2.getBytes(StandardCharsets.UTF_8);
                byte[] bytes3 = message3.getBytes(StandardCharsets.UTF_8);
                byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
                // Send 4-byte size header
                out.writeInt(bytes.length);
                // Send packet data
                out.write(bytes1);
                out.flush();
                sleep(1000);
                out.write(bytes2);
                out.flush();
                sleep(1000);
                out.write(bytes3);
                out.flush();
                System.out.println("Sent: " + message);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
