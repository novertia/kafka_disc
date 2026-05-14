package main.java.com.example.kafkadisc.Storage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;

public class LogStorage {
    private static final String BASE_DIR = "/tmp/kafka_disc/";
    private static final long MAX_FILE_SIZE = 1024 * 1024 * 1024; // 1 GB
    private static final String LOG_SUFFIX = ".log";

    private File currentFile;
    private long currentFileStartOffset; // The logical offset the current file starts with
    private long currentLogicalOffset;    // The logical offset for the next record

    public LogStorage() {
        initialize();
    }

    private void initialize() {
        File dir = new File(BASE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        File[] files = dir.listFiles((d, name) -> name.endsWith(LOG_SUFFIX));
        if (files == null || files.length == 0) {
            this.currentFileStartOffset = 0;
            this.currentLogicalOffset = 0;
            this.currentFile = new File(dir, formatFilename(0));
        } else {
            // Sort to find the latest file
            Arrays.sort(files, (f1, f2) -> f1.getName().compareTo(f2.getName()));
            this.currentFile = files[files.length - 1];
            this.currentFileStartOffset = getOffsetFromFilename(currentFile.getName());
            this.currentLogicalOffset = calculateCurrentOffset(currentFile);
        }
    }

    /**
     * Iterates through the file to count records and determine the next logical offset.
     */
    private long calculateCurrentOffset(File file) {
        long offsetCount = currentFileStartOffset;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long length = raf.length();
            long pos = 0;
            while (pos < length) {
                if (pos + 4 > length) break; // Incomplete header
                int size = raf.readInt();
                pos += 4 + size;
                raf.seek(pos);
                offsetCount++;
            }
        } catch (IOException e) {
            System.err.println("Warning: Error recovering offset from " + file.getName() + ": " + e.getMessage());
        }
        return offsetCount;
    }

    public void append(String payload) throws IOException {
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        int payloadSize = bytes.length;
        int totalSizeToAdd = 4 + payloadSize;

        // Roll file if 1GB limit exceeded
        if (currentFile.exists() && currentFile.length() + totalSizeToAdd > MAX_FILE_SIZE) {
            currentFileStartOffset = currentLogicalOffset;
            currentFile = new File(BASE_DIR, formatFilename(currentFileStartOffset));
        }

        try (RandomAccessFile raf = new RandomAccessFile(currentFile, "rw");
             FileChannel channel = raf.getChannel()) {
            
            long currentPosition = raf.length();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, currentPosition, totalSizeToAdd);
            
            buffer.putInt(payloadSize);
            buffer.put(bytes);
            
            // buffer.force() removed for performance; OS will manage flushing
            currentLogicalOffset++;
        }
    }

    /**
     * Finds the message at the given logical offset and transfers it to the socket channel.
     * Transfers the 4-byte size header followed by the payload.
     */
    public boolean consume(long offset, SocketChannel channel) throws IOException {
        File fileToRead = findFileForOffset(offset);
        if (fileToRead == null || !fileToRead.exists()) {
            return false;
        }

        long fileBaseOffset = getOffsetFromFilename(fileToRead.getName());
        
        try (RandomAccessFile raf = new RandomAccessFile(fileToRead, "r");
             FileChannel fileChannel = raf.getChannel()) {
            
            long fileLength = raf.length();
            long position = 0;
            long currentOffset = fileBaseOffset;

            // Scan to find the physical position of the logical offset
            while (currentOffset < offset && position < fileLength) {
                if (position + 4 > fileLength) return false;
                raf.seek(position);
                int size = raf.readInt();
                position += 4 + size;
                currentOffset++;
            }

            if (currentOffset == offset && position + 4 <= fileLength) {
                raf.seek(position);
                int payloadSize = raf.readInt();
                long totalRecordSize = 4 + (long) payloadSize;

                // Transfer the 4-byte size header AND the payload directly from the file
                long transferred = 0;
                while (transferred < totalRecordSize) {
                    long bytesSent = fileChannel.transferTo(position + transferred, totalRecordSize - transferred, channel);
                    if (bytesSent <= 0) break; // Channel might be full or closed
                    transferred += bytesSent;
                }
                return transferred == totalRecordSize;
            }
        }
        return false;
    }

    private File findFileForOffset(long offset) {
        File dir = new File(BASE_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(LOG_SUFFIX));
        if (files == null || files.length == 0) return null;

        Arrays.sort(files, Comparator.comparing(File::getName));
        
        File candidate = null;
        for (File file : files) {
            long base = getOffsetFromFilename(file.getName());
            if (base <= offset) {
                candidate = file;
            } else {
                break;
            }
        }
        return candidate;
    }

    private String formatFilename(long offset) {
        return String.format("%020d%s", offset, LOG_SUFFIX);
    }

    private long getOffsetFromFilename(String filename) {
        try {
            return Long.parseLong(filename.replace(LOG_SUFFIX, ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public long getCurrentLogicalOffset() {
        return currentLogicalOffset;
    }
}
