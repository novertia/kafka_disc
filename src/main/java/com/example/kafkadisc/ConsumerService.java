package main.java.com.example.kafkadisc;

import main.java.com.example.kafkadisc.Requests.FetchRequest;
import main.java.com.example.kafkadisc.Requests.FetchResponse;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ConsumerService {
    void getData(ClientSession session, QueueService queueService, ResponseService responseService, SocketChannel channel) throws IOException {
        FetchRequest fetchRequest = session.readAndClear();
        if(fetchRequest != null && fetchRequest.getFetch()){
            String payload = queueService.returnQueueData();
            if (queueService.returnQueueData() != null)
                responseService.sendFetchedData(new FetchResponse(payload, true), session, channel);
            else {
                responseService.sendFetchedData(new FetchResponse(null, false), session, channel);
            }
        }
    }
}
