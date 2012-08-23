package net.visualcoding.ts3serverquery;

import java.lang.StringBuilder;
import java.lang.Thread;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.ArrayList;

public class TS3InputThread extends Thread {
    private TS3ServerQueryClient serverQuery;
    private BlockingQueue<String> queue;
    private InputStream stream;

    public TS3InputThread(TS3ServerQueryClient serverQuery, InputStream stream) {
        this.serverQuery = serverQuery;
        this.stream = stream;

        // Instantiate our blocking queue
        queue = new ArrayBlockingQueue<String>(20);
    }

    public void run() {
        // Instantiate a TS3 Reader object
        TS3Reader reader = new TS3Reader(new InputStreamReader(stream));

        try {
            // Skip the first 2 lines (as they are just a welcome message)
            for(int i = 0; i < 2; i++) reader.readLine();

            // Read in input
            String input;
            while((input = reader.readLine()) != null) {
                if(input.startsWith("notify")) {
                    // Send to the server query to handle notification
                    serverQuery.notify(input);
                    continue;
                } else {
                    // Add input to our queue, blocking if full
                    queue.put(input);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try { reader.close(); } catch(Exception e) { }
        }
    }

    public String[] nextResponse() throws InterruptedException {
        // Create an ArrayList to store results
        ArrayList<String> list = new ArrayList<String>(20);

        // Read until we get our error message
        while(true) {
            String message = queue.take();
            list.add(message);

            // Stop once we found our error message
            if(message.startsWith("error")) break;
        }

        return list.toArray(new String[list.size()]);
    }
}
