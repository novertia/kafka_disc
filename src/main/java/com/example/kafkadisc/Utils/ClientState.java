package main.java.com.example.kafkadisc.Utils;

public enum ClientState {
    PRODUCER,
    CONSUMER,
    UNKNOWN;

    public static ClientState fromOrdinal(int n) throws IllegalArgumentException {
        // Safety check to prevent ArrayIndexOutOfBoundsException
        if (n < 0 || n >= ClientState.values().length - 1) {
            throw new IllegalArgumentException("Invalid ordinal");
        }
        return ClientState.values()[n];
    }
}
