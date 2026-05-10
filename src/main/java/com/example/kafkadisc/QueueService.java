package main.java.com.example.kafkadisc;

import java.util.ArrayDeque;
import java.util.Queue;

public class QueueService {
    private static Queue<String> q;

    QueueService(){
        q = new ArrayDeque<>();
    }

    void pushToQueue(String s){
        q.offer(s);
    }

    String returnQueueData(){
        if(!q.isEmpty()){
            String data = q.peek();
            q.poll();
            return data;
        }
        return null;
    }

}
