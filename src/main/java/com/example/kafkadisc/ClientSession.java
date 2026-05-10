package main.java.com.example.kafkadisc;

import lombok.Data;
import main.java.com.example.kafkadisc.Requests.Packet;
import main.java.com.example.kafkadisc.Requests.Produce;
import main.java.com.example.kafkadisc.Utils.ClientState;

import java.nio.ByteBuffer;

@Data
public class ClientSession {
    // 100MB buffer for reading
    private final ByteBuffer buffer = ByteBuffer.allocate(100 * 1024 * 1024);
    // 100MB buffer for writing
    private final ByteBuffer responseBuffer = ByteBuffer.allocate(100 * 1024 * 1024);
    ClientState state = ClientState.UNKNOWN;
    private long lastActiveTime;

    public ClientSession() {
        this.lastActiveTime = System.currentTimeMillis();
        this.responseBuffer.flip(); // Start in read mode for the channel.write()
    }

    public void updateTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }

    public boolean isExpired(int ttlMs) {
        return System.currentTimeMillis() - this.lastActiveTime > ttlMs;
    }

    public void queueResponse(byte[] data) {
        responseBuffer.clear(); // Reset position to 0 and limit to capacity for writing
        responseBuffer.put(data);
        responseBuffer.flip(); // Prepare for sending: limit = current position, position = 0
    }

    public void clearResponse() {
        responseBuffer.clear();
        responseBuffer.flip(); // Set limit to 0 so hasRemaining() is false
    }

    public boolean hasResponse() {
        return responseBuffer.hasRemaining();
    }

    /**
     * Reads all complete packets from the buffer.
     * Each packet must have a 4-byte size header (int).
     * Size must be < 1MB.
     * Partial packets or partial headers are kept in the buffer for the next read.
     */
    public <T extends Packet> T readAndClear(){
        buffer.flip(); // Switch to read mode
        T packet = null;
        try {
            if (buffer.remaining() >= 4) {
                buffer.mark(); // Mark position before size header
                int size = buffer.getInt();

                if (buffer.remaining() < size) {
                    // Full packet not yet available
                    buffer.reset(); // Move back to before size header
                }
                else {
                    // Read full packet
                    byte[] bytes = new byte[size];
                    buffer.get(bytes);
                    if (size >= 1024 * 1024) {
                        System.err.println("Error: Packet size " + size + " is invalid or exceeds 1MB limit.");
                    }
                    else{
                        packet = Serializer.decode(bytes);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Exception during packet processing: " + e.getMessage());
            buffer.clear();
            return packet;
        }

        // Keep partial data in the buffer
        buffer.compact();
        return packet;
    }

}
