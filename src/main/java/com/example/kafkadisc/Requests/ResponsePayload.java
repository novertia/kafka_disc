package main.java.com.example.kafkadisc.Requests;

import java.io.Serializable;

public class ResponsePayload implements Packet {
    public boolean success;

    public ResponsePayload(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "ResponsePayload{success=" + success + "}";
    }
}
