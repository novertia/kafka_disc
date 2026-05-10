package main.java.com.example.kafkadisc.Requests;


import lombok.Data;
import main.java.com.example.kafkadisc.Serializer;

import java.io.Serializable;
@Data
public class Produce implements Packet {
    public String data;

    public Produce(String data) {
        this.data = data;
    }

}
