package net.visualcoding.ts3serverquery;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.ArrayList;

/**
 * This class implements at thread that listens for input from the Teamspeak 3
 * Server. This class redirects input from the Teamspeak 3 Server to it's
 * associated instance of a {@link TS3ServerQueryClient} object.
 * <p>
 * It also manages notifications by calling
 * {@link TS3ServerQueryClient#notify(String)} upon notification. In addition,
 * this class provides the {@link #nextResponse()} method to wait for the
 * Teamspeak 3 Server to send a response after receiving a command.
 *
 * @author Aldehir Rojas
 * @version 1.0
 */
public class TS3InputThread extends Thread {

    /** Server Query Client to send input. */
    private TS3ServerQueryClient serverQuery;

    /**
     * Blocking queue used to communicate between the listening thread and
     * the thread requesting the Teamspeak 3 server response.
     */
    private BlockingQueue<String> queue;

    /** Input stream to read from. */
    private InputStream stream;

    /**
     * Constructs an input thread associated with the specified server query
     * client and input stream with a queue size of 20.
     *
     * @param serverQuery Server query client to associate this thread with
     * @param stream      Input stream this thread should read from
     */
    public TS3InputThread(TS3ServerQueryClient serverQuery,
            InputStream stream) {
        this(serverQuery, stream, 20);
    }

    /**
     * Constructs an input thread associated with the specified server query
     * client, input stream, and queue size.
     *
     * @param serverQuery Server query client to associate this thread with
     * @param stream      Input steam this thread should read from
     * @param queueSize   Size of the blocking queue
     */
    public TS3InputThread(TS3ServerQueryClient serverQuery,
            InputStream stream, int queueSize) {
        this.serverQuery = serverQuery;
        this.stream = stream;

        // Instantiate our blocking queue
        queue = new ArrayBlockingQueue<String>(queueSize);
    }

    /**
     * Executes the listening thread.
     */
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

    /**
     * Returns an array of strings that are queued in the blocking queue up
     * to the first Teamspeak 3 server query error message.
     *
     * @return an array of strings that are queued in the internal blocking
     *         queue up to the first Teamspeak 3 server query error message
     *
     * @throws InterruptedException
     */
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
