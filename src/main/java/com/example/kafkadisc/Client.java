package main.java.com.example.kafkadisc;

import java.io.*;
import java.net.*;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        for(int i=1; i<=4; i++) {
            try (Socket socket = new Socket(HOST, PORT)) {
                System.out.println("Connected to server");

                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println("Hello from Client!" + i);

                // Client logic for receiving file or response goes here
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
