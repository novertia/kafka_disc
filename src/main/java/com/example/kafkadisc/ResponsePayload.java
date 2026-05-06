package main.java.com.example.kafkadisc;

import java.io.Serializable;

public class ResponsePayload implements Serializable {
    public boolean success;

    public ResponsePayload(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "ResponsePayload{success=" + success + "}";
    }
}
