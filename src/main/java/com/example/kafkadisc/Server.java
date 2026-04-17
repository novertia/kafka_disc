package com.example.kafkadisc;

import java.io.*;
import java.net.*;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Server {
    private static final int PORT = 8080;
    private static final int TIMEOUT_MS = 10000; // 10 seconds

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    // Set timeout for socket operations
                    clientSocket.setSoTimeout(TIMEOUT_MS);
                    System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());

                    // Handle client (for now just a simple message)
                    InputStream input = clientSocket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    String line = reader.readLine();
                    System.out.println("Received: " + line);

                    // Placeholder for sendFile() logic using FileChannel.transferTo
                    // sendFile(clientSocket, Path.of("somefile.dat"));
                } catch (SocketTimeoutException e) {
                    System.err.println("Socket timed out: " + e.getMessage());
                } catch (IOException e) {
                    System.err.println("Error handling client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Future placeholder for sendFile using zero-copy transferTo
     */
    private static void sendFile(Socket socket, Path filePath) throws IOException {
        try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            long size = fileChannel.size();
            long position = 0;
            // Java's equivalent to sendfile() syscall
            while (position < size) {
                position += fileChannel.transferTo(position, size - position, socket.getChannel());
            }
        }
    }
}
