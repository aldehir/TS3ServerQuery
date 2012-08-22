package net.visualcoding.ts3serverquery;

import net.visualcoding.ts3serverquery.event.*;

import java.net.Socket;
import java.lang.Runnable;
import java.util.Arrays;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;

/**
 * TS3ServerQuery is a low-level interface for communicating with a Teamspeak 3
 * server through the Teamspeak 3 Server Query.
 * 
 * @author Aldehir Rojas
 * @version 1.0
 */
public class TS3ServerQuery {

    /** Socket connection to the TS3 server */
    private Socket connection;

    /** Input thread */
    private TS3InputThread inputThread;

    /** Output thread */
    private TS3OutputThread outputThread;

    /** Polling thread */
    private TS3PollingThread pollingThread;

    /** Semaphore to ensure that only one command is sent at a time */
    private Semaphore commandMutex;

    /** Teamspeak 3 Server Host */
    private String host;
    
    /** Teamspeak 3 Server Port */
    private int port;

    /** Executor Service to handle event threads */
    private ExecutorService executorService;
    
    /** Event listeners */
    private ArrayList<TS3EventListener> eventListeners;

    /**
     * Constructor. Initializes the TS3ServerQuery with the Teamspeak 3 host and
     * port.
     *
     * @param host Teamspeak 3 Server Host
     * @param port Teamspeak 3 Server Port
     */
    public TS3ServerQuery(String host, int port) {
        // Set connection settings
        setHost(host);
        setPort(port);

        // Initialize semaphores/mutexes
        commandMutex = new Semaphore(1);

        // Instantiate our list of event listeners
        eventListeners = new ArrayList<TS3EventListener>();

        // Create the executor service used to manage threads for notifications
        executorService = Executors.newCachedThreadPool();
    }

    /**
     * Get the Teamspeak 3 Server Host
     * @return Teamspeak 3 Server Host
     */
    public String getHost() {
        return host;
    }

    /**
     * Get the Teamspeak 3 Server Port
     * @return Teamspeak 3 Server Port
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the Teamspeak 3 Server Host
     * @param host Teamspeak 3 Server Host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Set the Teamspeak 3 Server Port
     * @param port Teamspeak 3 Server Port
     */
    public void setPort(int port) {
        this.port = port;
    }

    public boolean connect() {
        try {
            // Open up a connection to the TS3 Server Query (telnet)
            connection = new Socket(host, port);

            // Create our input/output threads
            inputThread = new TS3InputThread(this, connection.getInputStream());
            outputThread = new TS3OutputThread(this, connection.getOutputStream());

            // Start them up!
            inputThread.start();
            outputThread.start();
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public TS3Result execute(String command) throws InterruptedException {
        if(command.isEmpty()) return null;

        commandMutex.acquire(); // Only allow one command to execute at a time

        outputThread.send(command);
        String[] response = inputThread.nextResponse();
        TS3Result result = new TS3Result(response);

        commandMutex.release();

        return result;
    }

    public TS3Result execute(TS3Command command) throws InterruptedException {
        return execute(command.toString());
    }

    public boolean registerNotifications() {
        boolean allSuccessful = true;
        String[] events = { "textserver", "textchannel", "textprivate" };

        // Register events
        for(int i = 0; i < events.length; i++) {
            TS3Command command = new TS3Command("servernotifyregister");
            command.add("event", events[i]);

            try {
                TS3Result result = execute(command);
                if(result.hasError()) allSuccessful = false;
            } catch(Exception e) {
                allSuccessful = false;
            }
        }

        // Start our polling thread
        pollingThread = new TS3PollingThread(this);
        pollingThread.start();

        return allSuccessful;
    }

    protected void notify(String notification) {
        // Split into the notification type and it's values
        String[] parts = notification.split("\\s+", 2);
        Map<String, String> values = TS3Util.parseDetails(parts[1]);

        try {
            if(parts[0].equalsIgnoreCase("notifytextmessage")) {
                int id = Integer.parseInt(values.get("invokerid"));
                int mode = Integer.parseInt(values.get("targetmode"));

                TS3MessageEvent event = new TS3MessageEvent(
                    values.get("invokername"),
                    id,
                    values.get("invokeruid")
                );

                event.setMode(mode);
                event.setMessage(values.get("msg"));

                notify(event);
            }
        } catch(Exception e) {
            // ...
        }
    }

    protected void notify(final TS3Event event) {
        // Don't spawn a thread if we have no event listeners
        synchronized(eventListeners) {
           if(eventListeners.size() == 0) return;
        }

        // Create a runnable object to execute the event on all listeners
        Runnable task = new Runnable() {
            public void run() {
                synchronized(eventListeners) {
                    for(TS3EventListener listener : eventListeners) {
                        event.execute(listener);
                    }
                }
            }
        };

       // Now submit our runnable to our executor service
       executorService.submit(task);
    }

    public void addEventListener(TS3EventListener listener) {
        synchronized(eventListeners) {
            eventListeners.add(listener);
        }
    }

    public static void main(String[] args) throws Exception {
        TS3ServerQuery q = new TS3ServerQuery("localhost", 10011);
        q.connect();

        // Command: login serveradmin UyN35cJO
        TS3Result result = q.execute("login serveradmin UyN35cJO");
        System.out.println(result);

        // Command: use sid=1
        TS3Command use = (new TS3Command("use")).add("sid", 1);
        result = q.execute(use);
        System.out.println(result);

        // Register all notifications
        if(q.registerNotifications()) {
            System.out.println("Registered for notifications");
        }

        // Create an event listener
        TS3EventListener listener = new TS3EventListener() {
            public void onClientConnected(TS3ClientConnectedEvent event) {
                System.out.println("Client connected: " + event.getClientName());
            }

            public void onClientDisconnected(TS3ClientDisconnectedEvent event) {
                System.out.println("Client disconnected: " + event.getClientName());
            }

            public void onClientMoved(TS3ClientMovedEvent event) {
                System.out.println("Client moved: " + event.getClientName() +
                        " from " + event.getSource() + " to " + event.getDestination());
            }

            public void onMessage(TS3MessageEvent event) {
                System.out.println("Message received from: " + event.getClientName());
            }
        };

        // Add listener
        q.addEventListener(listener);


        // Spawn other threads to execute commands (and see how it works with our polling thread)
        /*
        while(true) {
            Thread t = new Thread(new RunnableTest(q));
            t.start();

            try { Thread.sleep(20); }
            catch(Exception e) { }
        }
        */
    }

    private static class RunnableTest implements Runnable {
        private TS3ServerQuery serverQuery;

        public RunnableTest(TS3ServerQuery serverQuery) {
            this.serverQuery = serverQuery;
        }

        public void run() {
            try {
                // Execute a command... like "clientlist"
                TS3Result result = serverQuery.execute("clientlist");
                if(!result.hasError()) {
                    System.out.println("Thread executed");
                }
            } catch(Exception e) { }
        }
    }
}
