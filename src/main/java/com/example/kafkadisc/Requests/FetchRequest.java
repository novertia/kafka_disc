package main.java.com.example.kafkadisc.Requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import main.java.com.example.kafkadisc.Serializer;

import java.io.Serializable;
@Data
@AllArgsConstructor
public class FetchRequest implements Packet, Serializable {
    Boolean fetch;
}