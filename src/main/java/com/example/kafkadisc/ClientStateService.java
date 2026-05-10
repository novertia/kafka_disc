package main.java.com.example.kafkadisc;

import main.java.com.example.kafkadisc.Requests.StateInfo;
import main.java.com.example.kafkadisc.Utils.ClientState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientStateService {

    void sessionStateChange(ClientSession session, ResponseService responseService, SocketChannel channel) throws IOException{
        StateInfo info = session.readAndClear();
        if (info == null){
            return;
        }
        boolean success = false;
        try {
            ClientState state = ClientState.fromOrdinal(info.getClientState());
            System.out.println("Received state change to" + state);
            session.setState(state);
            success = true;
        }
        catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        responseService.sendSimpleAck(success, session, channel);
    }
}
