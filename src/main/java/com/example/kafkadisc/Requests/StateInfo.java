package main.java.com.example.kafkadisc.Requests;

import lombok.Data;
import main.java.com.example.kafkadisc.Utils.ClientState;

import java.io.Serializable;


@Data
public class StateInfo implements Packet, Serializable {
    int clientState;

    public StateInfo(ClientState state){
        this.clientState = state.ordinal();
    }
}
