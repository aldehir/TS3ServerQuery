package net.visualcoding.ts3serverquery;

import net.visualcoding.ts3serverquery.event.TS3Event;
import net.visualcoding.ts3serverquery.event.TS3MessageEvent;
import net.visualcoding.ts3serverquery.event.TS3ClientMovedEvent;
import net.visualcoding.ts3serverquery.event.TS3ClientConnectedEvent;
import net.visualcoding.ts3serverquery.event.TS3ClientDisconnectedEvent;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This thread listens for events and executes them all under a single thread.
 * Prior to 1.1.0, every event was handled in it's own thread. Unfortunately,
 * this approach would require event listeners to implement synchronization
 * of shared resources on their own.
 *
 * @author Aldehir Rojas
 * @version 1.1.0
 */
public class TS3EventThread extends Thread {

    /** The default size of the blocking queue. */
    public static final int DEFAULT_QUEUE_SIZE = 25;

    /** Server query client that spawned this event thread. */
    private TS3ServerQueryClient serverQueryClient;

    /** Blocking queue containing events that need to be handled. */
    private BlockingQueue<TS3Event> eventQueue;

    /** Event listeners. */
    private List<TS3EventListener> listeners;

    /**
     * Constructs an event thread with a queue size of {@value
     * DEFAULT_QUEUE_SIZE}.
     *
     * @param client Server query client that spawns this thread.
     */
    public TS3EventThread(TS3ServerQueryClient client) {
        this(client, DEFAULT_QUEUE_SIZE);
    }

    /**
     * Constructs an event thread with the specified queue size.
     *
     * @param client Server query client that spawns this thread.
     * @param queueSize size of the bounded blocking queue used to store events
     */
    public TS3EventThread(TS3ServerQueryClient client, int queueSize) {
        this.serverQueryClient = client;

        // Use a synchronized list to store our listeners
        listeners = Collections.synchronizedList(
                new LinkedList<TS3EventListener>());

        // Blocking queue for events
        eventQueue = new ArrayBlockingQueue(queueSize);
    }

    /**
     * Adds an event listener to receive events from this thread.
     *
     * @param listener event listener to receive events from this thread.
     * @return {@code true} if the listener was added, {@code false} if the
     *         listener is already registered.
     */
    public boolean addListener(TS3EventListener listener) {
        // Check to see if the reference to the specified listener is already
        // in the list.
        boolean alreadyRegistered = false;
        Iterator<TS3EventListener> it = listeners.iterator();
        while(it.hasNext()) {
            if(it.next() == listener) {
                return false;
            }
        }

        // Only add the listener if a reference of the same object
        // does not already exist in our list.
        if(!alreadyRegistered) listeners.add(listener);
        return true;
    }

    /**
     * Removes the event listener with the same reference as the specified
     * listener from this thread.
     *
     * @param listener event listener to remove from this thread.
     * @return {@code true} if the listener was removed, {@code false} if the
     *         listener object was not registered in this thread.
     */
    public boolean removeListener(TS3EventListener listener) {
        // Remove the first item in the list with the same reference as the
        // specified listener.
        Iterator<TS3EventListener> it = listeners.iterator();
        while(it.hasNext()) {
            if(it.next() == listener) {
                it.remove();
                return true;
            }
        }

        return false;
    }

    /**
     * Executes this event thread.
     */
    public void run() {
        try {

            // Loop through all events in the queue, blocking until an event
            // is present.
            TS3Event event;
            while((event = eventQueue.take()) != null) {

                // Execute the event for all of the listeners
                for(TS3EventListener listener : listeners) {
                    event.execute(listener);
                }

            }

        } catch(InterruptedException e) {
            serverQueryClient.getLogger().debug("Event thread interrupted");
        }

        serverQueryClient.getLogger().info("Event thread terminated");
    }

    /**
     * Notify the event listeners that an event has occurred.
     * @param notification Raw notification string from the TS3 Server Query.
     */
    public void notify(String notification) {
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
     * Notify the event thread that an event has occurred.
     * @param event TS3Event to queue for handling.
     */
    public void notify(final TS3Event event) {
        serverQueryClient.getLogger().debug(event.toString());

        try {
            // Queue the event
            if(!eventQueue.offer(event, 5, TimeUnit.SECONDS)) {
                serverQueryClient.getLogger().error(
                        "Unable to queue event due to timeout");
            }
        } catch(InterruptedException e) {
            // ... Do nothing
        }
    }
}
