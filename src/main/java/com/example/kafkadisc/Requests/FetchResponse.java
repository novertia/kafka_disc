package main.java.com.example.kafkadisc.Requests;

public class FetchResponse implements Packet {
    public Boolean dataPresent;
    public String payload;

    public FetchResponse(String payload, Boolean dataPresent){
        this.dataPresent = dataPresent;
        this.payload = payload;
    }
}
