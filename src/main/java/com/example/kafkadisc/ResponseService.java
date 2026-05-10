package main.java.com.example.kafkadisc;

import main.java.com.example.kafkadisc.Requests.FetchResponse;
import main.java.com.example.kafkadisc.Requests.ResponsePayload;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ResponseService {
    void sendSimpleAck(boolean success, ClientSession session, SocketChannel channel) throws IOException {
        ResponsePayload response = new ResponsePayload(success);
        byte[] responseBytes = Serializer.encode(response);
        session.queueResponse(responseBytes);
        sendDataToChannel(session, channel);
        // Write response directly to the channel

    }

    void sendFetchedData(FetchResponse response, ClientSession session, SocketChannel channel) throws  IOException {
        byte[] responseBytes = Serializer.encode(response);
        session.queueResponse(responseBytes);
        sendDataToChannel(session, channel);
    }

    private void sendDataToChannel(ClientSession session, SocketChannel channel) throws IOException{
        ByteBuffer respBuf = session.getResponseBuffer();
        while (respBuf.hasRemaining()) {
            channel.write(respBuf);
        }
    }
}
