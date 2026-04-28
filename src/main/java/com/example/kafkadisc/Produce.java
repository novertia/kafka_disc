package main.java.com.example.kafkadisc;


import java.io.Serializable;

public class Produce implements Serializable {
    String data;

    public Produce(String data) {
        this.data = data;
    }
}
