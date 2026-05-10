package main.java.com.example.kafkadisc;

import main.java.com.example.kafkadisc.Requests.*;
import main.java.com.example.kafkadisc.Utils.ClientState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class Consumer {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        for(int i=1; i<=1; i++) {
            try (Socket socket = new Socket(HOST, PORT)) {
                System.out.println("Connected to server");

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                StateInfo state = new StateInfo(ClientState.CONSUMER);

                FetchRequest request = new FetchRequest(true);
                byte[] stateByte = Serializer.encode(state);
                byte[] fetch1 = Serializer.encode(request);
                byte[] fetch2 = Serializer.encode(request);
                // Send 4-byte size header
                // Send packet data
                DataInputStream in = new DataInputStream(socket.getInputStream());

                out.write(stateByte);
                out.flush();
                readAck(in);

                out.write(fetch1);
                out.flush();
                readResponse(in);
                
                sleep(1000);
                out.write(fetch2);
                out.flush();
                readResponse(in);

                
                System.out.println("Received 2 consumed payloads");

            } catch (IOException | InterruptedException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static void readAck(DataInputStream in) throws IOException, ClassNotFoundException{
        int responseSize = in.readInt();
        byte[] responseBytes = new byte[responseSize];
        in.readFully(responseBytes);
        ResponsePayload response = Serializer.decode(responseBytes);
        System.out.println("Received ack for consumer: " + response);
    }

    private static void readResponse(DataInputStream in) throws IOException, ClassNotFoundException {
        int responseSize = in.readInt();
        byte[] responseBytes = new byte[responseSize];
        in.readFully(responseBytes);
        FetchResponse response = Serializer.decode(responseBytes);
        if(response != null && response.dataPresent)
            System.out.println("Received response: " + response.payload);
    }
}
