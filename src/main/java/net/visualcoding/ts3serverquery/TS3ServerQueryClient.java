package net.visualcoding.ts3serverquery;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.visualcoding.ts3serverquery.event.TS3Event;
import net.visualcoding.ts3serverquery.event.TS3MessageEvent;

/**
 * TS3ServerQuery is a low-level interface for communicating with a Teamspeak 3
 * server through the Teamspeak 3 Server Query.
 *
 * @author Aldehir Rojas
 * @version 1.0
 *
 * @todo Implement handling of server and channel notifications, i.e. when
 *       the polling thread is not spawned.
 */
public class TS3ServerQueryClient {

    /** Socket connection to the TS3 server. */
    private Socket connection = null;

    /** Input thread. */
    private TS3InputThread inputThread = null;

    /** Writer object for sending commands. */
    private TS3Writer writer = null;

    /** Polling thread. */
    private TS3PollingThread pollingThread = null;

    /** Semaphore to ensure that only one command is sent at a time. */
    private Semaphore commandMutex;

    /** Teamspeak 3 Server Host. */
    private String host;

    /** Teamspeak 3 Server Port. */
    private int port;

    /** Executor Service to handle event threads. */
    private ExecutorService executorService;

    /** Event listeners. */
    private ArrayList<TS3EventListener> eventListeners;

    /**
     * Logger. Use the SLF4J API because it allows the application implementing
     * this client to use whichever logging library it wishes.
     */
    private final Logger logger = LoggerFactory.getLogger(
            TS3ServerQueryClient.class);

    /**
     * Constructor. Initialize this TS3ServerQueryClient with the Teamspeak 3
     * server host and default server query port, 10011.
     *
     * @param host Teamspeak 3 Server Host
     */
    public TS3ServerQueryClient(String host) {
        this(host, 10011);
    }

    /**
     * Constructor. Initializes this TS3ServerQueryClient with the Teamspeak 3
     * server host and port.
     *
     * @param host Teamspeak 3 Server Host
     * @param port Teamspeak 3 Server Port
     */
    public TS3ServerQueryClient(String host, int port) {
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
     * Returns the Teamspeak 3 Server Host.
     * @return Teamspeak 3 Server Host
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the Teamspeak 3 Server Port.
     * @return Teamspeak 3 Server Port
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the Logger object used by this query client.
     * @return the Logger object used by this query client.
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Sets the Teamspeak 3 Server Host.
     * @param host Teamspeak 3 Server Host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Sets the Teamspeak 3 Server Port.
     * @param port Teamspeak 3 Server Port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Add an event listener.
     * @param listener Listener to add
     */
    public void addEventListener(TS3EventListener listener) {
        synchronized(eventListeners) {
            eventListeners.add(listener);
        }
    }

    /**
     * Connects to the TS3 Server through the Server Query interface.
     *
     * @throws IOException
     */
    public void connect() throws IOException {
        try {
            // Open up a connection to the TS3 Server Query (telnet)
            connection = new Socket(host, port);
        } catch(IOException e) {
            logger.info("Unable to establish connection to {}:{}", host,port);
            throw e;
        }

        logger.info("Connected to {}:{}", host, port);

        // Instantiate a writer object for sending output
        writer = new TS3Writer(new OutputStreamWriter(
                connection.getOutputStream()));

        // Create our input (listening) thread
        inputThread = new TS3InputThread(this, connection.getInputStream());

        // Start up the listening thread
        inputThread.start();
    }

    /**
     * Disconnects from the TS3 Server.
     */
    public void disconnect() throws IOException {
        // Close our socket, the threads will respond
        // to the thrown SocketException by terminating themselves
        if(connection != null) connection.close();
        connection = null;

        // Wait for our threads to finish terminating
        try {
            if(inputThread != null) inputThread.join();
            if(pollingThread != null) pollingThread.join();
        } catch(InterruptedException e) {
            logger.warn("Interrupted before threads finished");
        }

        logger.info("Disconnected from server");
    }

    /**
     * Executes a given command.
     *
     * @param command Command to execute
     * @return TS3Result containing the response of the command
     * @throws InterruptedException
     * @throws IOException
     *
     * @see #execute(String, TS3Map)
     */
    public TS3Result execute(String command)
            throws InterruptedException, IOException {
        if(command.isEmpty()) return null;

        /* Only allow one command to execute at a time, this way we don't
         * receive a response that is intended for another command. */
        commandMutex.acquire();

        // Send the command through our writer
        writer.writeLine(command);

        logger.debug("Sent: {}", command);

        // Wait for a response and enclose in a TS3Result object
        String[] response = inputThread.nextResponse();
        TS3Result result = new TS3Result(response);

        logger.debug("Recv: {} ({})", result.getErrorMessage(),
                result.getErrorCode());

        // Allow other commands to execute
        commandMutex.release();

        return result;
    }

    /**
     * Executes the specified command with the specified arguments.
     *
     * @param command   Name of the command to execute
     * @param arguments Arguments to pass to to the command
     * @return {@code TS3Result} object containing the response of the
     *         specified command
     *
     * @throws InterruptedException
     * @throws IOException
     *
     * @see #execute(String)
     */
    public TS3Result execute(String command, TS3Map arguments)
            throws InterruptedException, IOException {
        if(arguments == null || arguments.isEmpty())
            return execute(command);
        return execute(command + " " + arguments.toString());
    }

    /**
     * Registers the TS3 notifications for receiving text messages and basic
     * user notifications. This is the equivalent to calling
     * {@code registerNotifications(true)}.
     *
     * @return True if all the notifications were successfully registered.
     * @see #registerNotifications(boolean)
     */
    public boolean registerNotifications() {
        return registerNotifications(true);
    }

    /**
     * Registers the TS3 notifications for receiving text messages and basic
     * user notifications. This method is a convenience method for registering
     * all the text message notifications. It also spawns a polling thread
     * that polls for the following events:
     * <ul>
     * <li>User connected</li>
     * <li>User disconnected</li>
     * <li>User moved</li>
     * </ul>
     * <p>
     * A polling thread is necessary to check if users move channels, as the
     * TS3 Server Query "channel" notifications only notifies if a user moves
     * in or out of the channel that the query client resides.
     * <p>
     * If {@code usePolling} is false, then the default "server" and "channel"
     * notifications are registered in place of the polling thread.
     *
     * @param usePolling Whether or not to spawn a polling thread
     * @return True if all the notifications were successfully registered.
     */
    public boolean registerNotifications(boolean usePolling) {
        boolean allSuccessful = true;

        // Add in the events for text messages
        ArrayList<String> events = new ArrayList<String>(5);
        events.add("textserver");
        events.add("textchannel");
        events.add("textprivate");

        // If we're not using polling, then register the server and channel
        // notifications
        if(!usePolling) {
            events.add("server");
            events.add("channel");
        }

        // Register events
        for(String event : events) {
            TS3Map arguments = new TS3Map();
            arguments.add("event", event);

            try {
                TS3Result result;

                // Handle the channel event differently
                if(event.equalsIgnoreCase("channel")) {
                    // Get our current channel id by calling "whoami"
                    result = execute("whoami");
                    String channelId = result.getFirst()
                            .get("client_channel_id");

                    // Add in the id to the command arguments
                    arguments.add("id", channelId);
                }

                // Execute the command
                result = execute("servernotifyregister", arguments);
                if(result.hasError()) allSuccessful = false;
            } catch(Exception e) {
                allSuccessful = false;
            }
        }

        // Start our polling thread if requested
        if(usePolling) {
            pollingThread = new TS3PollingThread(this);
            pollingThread.start();
        }

        return allSuccessful;
    }

    /**
     * Notify the event listeners that an event has occurred.
     * @param notification Raw notification string from the TS3 Server Query.
     */
    protected void notify(String notification) {
        // Split into the notification type and it's values
        String[] parts = notification.split("\\s+", 2);
        TS3Map map = new TS3Map(parts[1]);

        if(parts[0].equalsIgnoreCase("notifytextmessage")) {
            int id = map.getInteger("invokerid").intValue();
            int mode = map.getInteger("targetmode").intValue();

            TS3MessageEvent event = new TS3MessageEvent(
                map.get("invokername"),
                id,
                map.get("invokeruid")
            );

            event.setMode(mode);
            event.setMessage(map.get("msg"));

            notify(event);
        }
    }

    /**
     * Notify the event listeners that an event has occurred.
     * @param event TS3Event to send to the event listeners.
     */
    protected void notify(final TS3Event event) {
        logger.debug(event.toString());

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
}
