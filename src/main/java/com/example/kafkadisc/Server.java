package main.java.com.example.kafkadisc;

import main.java.com.example.kafkadisc.Requests.FetchResponse;
import main.java.com.example.kafkadisc.Requests.Produce;
import main.java.com.example.kafkadisc.Requests.ResponsePayload;
import main.java.com.example.kafkadisc.Storage.LogStorage;
import main.java.com.example.kafkadisc.Utils.ClientState;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 8080;
    private static final int TTL_MS = 600000; // 10 seconds TTL
    private static final int MAX_CONNECTIONS = 3;

    private static ProducerService producerService;
    private static ClientStateService clientStateService;
    private static ResponseService responseService;
    private static QueueService queueService;
    private static ConsumerService consumerService;
    private static LogStorage logStorage;


    public static void main(String[] args) {
        producerService = new ProducerService();
        clientStateService = new ClientStateService();
        responseService = new ResponseService();
        queueService = new QueueService();
        consumerService = new ConsumerService();
        logStorage = new LogStorage();
        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONNECTIONS);
        
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open();
             Selector selector = Selector.open()) {
            
            serverChannel.bind(new InetSocketAddress(PORT));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("NIO Server started on port " + PORT + " with " + MAX_CONNECTIONS + " max worker threads.");
            
            while (true) {
                // Wait for events or 1s timeout for TTL check
                if (selector.select(30000) == 0) {
                    checkTTL(selector);
                    continue;
                }

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) continue;

                    if (key.isAcceptable()) {
                        handleAccept(serverChannel, selector);
                    } else if (key.isReadable()) {
                        // Hand off to worker
                        System.out.println("Thi should be twice");
                        key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
                        executor.submit(() -> handleClient(key, selector));
                    }
                }
                
                // Also check TTL at the end of event processing
                checkTTL(selector);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    private static void handleAccept(ServerSocketChannel serverChannel, Selector selector) throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        ClientSession session = new ClientSession();
        clientChannel.register(selector, SelectionKey.OP_READ, session);
        System.out.println("Client connected: " + clientChannel.getRemoteAddress());
    }

    private static void handleClient(SelectionKey key, Selector selector){
        SocketChannel channel = (SocketChannel) key.channel();
        ClientSession session = (ClientSession) key.attachment();

        try {
            System.out.println("This should Souptik");
            int bytesRead = channel.read(session.getBuffer());
            System.out.println("This should after session:" + bytesRead);
            if (bytesRead == -1) {
                System.out.println("Client disconnected: " + channel.getRemoteAddress());
                closeConnection(key);
                return;
            }

            if (bytesRead > 0) {
                session.updateTime();
                System.out.println("Session state now" + session.getState());
                if(session.state == ClientState.UNKNOWN)
                    clientStateService.sessionStateChange(session, responseService, channel);
                else if(session.state == ClientState.PRODUCER){
                    System.out.println("Should be producer now");
                    producerService.produce(logStorage, session, responseService, channel);
                }
                else if(session.state == ClientState.CONSUMER){
                    consumerService.getData(session, logStorage, channel);
                }

            }

            if (key.isValid())
                key.interestOps(key.interestOps() | SelectionKey.OP_READ);

            selector.wakeup();

        } catch (IOException e) {
            System.err.println("IO Error reading from " + (key.isValid() ? "channel" : "closed key") + ": " + e.getMessage());
            closeConnection(key);
        }
    }

    private static void checkTTL(Selector selector) {
        for (SelectionKey key : selector.keys()) {
            if (key.attachment() instanceof ClientSession) {
                ClientSession session = (ClientSession) key.attachment();
                if (session.isExpired(TTL_MS)) {
                    try {
                        SocketChannel channel = (SocketChannel) key.channel();
                        System.out.println("Closing idle connection (TTL expired): " + channel.getRemoteAddress());
                        closeConnection(key);
                    } catch (IOException e) {
                        closeConnection(key);
                    }
                }
            }
        }
    }

    private static void closeConnection(SelectionKey key) {
        try {
            key.channel().close();
        } catch (IOException ignored) {}
        key.cancel();
    }


    /**
     * Placeholder for zero-copy file sending using NIO FileChannel
     */
    private static void sendFile(SocketChannel channel, Path filePath) throws IOException {
        try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            long size = fileChannel.size();
            long position = 0;
            while (position < size) {
                position += fileChannel.transferTo(position, size - position, channel);
            }
        }
    }
}
