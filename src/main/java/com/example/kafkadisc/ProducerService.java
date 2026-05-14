package main.java.com.example.kafkadisc;

import main.java.com.example.kafkadisc.Requests.Produce;
import main.java.com.example.kafkadisc.Storage.LogStorage;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ProducerService {

    void produce(LogStorage logStorage, ClientSession session, ResponseService responseService, SocketChannel channel) throws IOException {
        Produce producedPacket = session.readAndClear();
        if(producedPacket != null) {
            logStorage.append(producedPacket.getData());
            responseService.sendSimpleAck(true, session, channel);
        }
    }
}
