package main.java.com.example.kafkadisc;

import main.java.com.example.kafkadisc.Requests.FetchRequest;
import main.java.com.example.kafkadisc.Requests.FetchResponse;
import main.java.com.example.kafkadisc.Storage.LogStorage;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ConsumerService {
    void getData(ClientSession session, LogStorage storage, SocketChannel channel) throws IOException {
        FetchRequest fetchRequest = session.readAndClear();
        if(fetchRequest != null && fetchRequest.getFetch()){
            storage.consume(session.getOffset(), channel);
        }
        session.setOffset(session.getOffset() + 1);
    }
}
