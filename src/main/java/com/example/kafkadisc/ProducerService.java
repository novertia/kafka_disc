package main.java.com.example.kafkadisc;

import main.java.com.example.kafkadisc.Requests.Produce;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ProducerService {

    void produce(QueueService queueService, ClientSession session, ResponseService responseService, SocketChannel channel) throws IOException {
        Produce producedPacket = session.readAndClear();
        if(producedPacket != null) {
            queueService.pushToQueue(producedPacket.getData());
            responseService.sendSimpleAck(true, session, channel);
        }
    }
}
