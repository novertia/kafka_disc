package main.java.com.example.kafkadisc;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ClientSession {
    // 100MB buffer
    private final ByteBuffer buffer = ByteBuffer.allocate(100 * 1024 * 1024);
    private long lastActiveTime;

    public ClientSession() {
        this.lastActiveTime = System.currentTimeMillis();
    }

    public void updateTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }

    public boolean isExpired(int ttlMs) {
        return System.currentTimeMillis() - this.lastActiveTime > ttlMs;
    }

    /**
     * Reads all complete packets from the buffer.
     * Each packet must have a 4-byte size header (int).
     * Size must be < 1MB.
     * Partial packets or partial headers are kept in the buffer for the next read.
     */
    public List<Produce> readAndClear() {
        List<Produce> packets = new ArrayList<>();
        buffer.flip(); // Switch to read mode

        try {
            while (buffer.remaining() >= 4) {
                buffer.mark(); // Mark position before size header
                int size = buffer.getInt();

                if (buffer.remaining() < size) {
                    // Full packet not yet available
                    buffer.reset(); // Move back to before size header
                    break;
                }

                // Read full packet
                byte[] bytes = new byte[size];
                buffer.get(bytes);
                if (size >= 1024 * 1024) {
                    System.err.println("Error: Packet size " + size + " is invalid or exceeds 1MB limit.");
                    continue;
                }
                Produce packet = Serializer.decode(bytes);
                packets.add(packet);
            }
        } catch (Exception e) {
            System.err.println("Exception during packet processing: " + e.getMessage());
            buffer.clear();
            return packets;
        }

        // Keep partial data in the buffer
        buffer.compact();
        return packets;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
}
