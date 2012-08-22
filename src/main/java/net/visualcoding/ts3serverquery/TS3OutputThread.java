package net.visualcoding.ts3serverquery;

import java.lang.Thread;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class TS3OutputThread extends Thread {
    private TS3ServerQuery serverQuery;
    private BlockingQueue<String> queue;
    private OutputStream stream;

    public TS3OutputThread(TS3ServerQuery serverQuery, OutputStream stream) {
        this.serverQuery = serverQuery;
        this.stream = stream;

        // Instantiate our blocking queue
        queue = new LinkedBlockingQueue<String>();
    }

    public void run() {
        // Create our writer object
        TS3Writer writer = new TS3Writer(new OutputStreamWriter(stream));

        while(true) {
            try {
                // Block until a message is provided to us
                String message = queue.take();

                // Send it through our writer
                writer.write(message);
                writer.newLine();
                writer.flush();
            } catch(InterruptedException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(String message) throws InterruptedException {
        // Add the message to our queue, waiting if necessary
        queue.put(message);
    }
}
