package net.visualcoding.ts3serverquery.event;

import net.visualcoding.ts3serverquery.TS3EventListener;

/**
 * This is an abstract class for TS3 events containing the basic fields that
 * are present in all TS3 events.
 *
 * @author Aldehir Rojas
 * @version 1.0.0
 */
abstract public class TS3Event {

    /** Name of the client that triggered this event. */
    private String clientName;

    /** Id of the client that triggered this event. */
    private int clientId;

    /** Unique Id of the client that triggered this event. */
    private String clientUid;

    /**
     * Constructs a TS3Event object, initializing all String fields to
     * {@code null} and int fields to 0.
     */
    public TS3Event() {
        this(null, 0, null);
    }

    /**
     * Constructs a TS3Event object, initializing all fields to their specified
     * values.
     *
     * @param clientName name of the client that triggered this event
     * @param clientId id of the client that triggered this event
     * @param clientUid unique id of the client that triggered this event
     */
    public TS3Event(String clientName, int clientId, String clientUid) {
        setClientName(clientName);
        setClientId(clientId);
        setClientUid(clientUid);
    }

    /**
     * Returns the name of the client that triggered this event.
     * @return the name of the client that triggered this event
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * Returns the id of the client that triggered this event.
     * @return the id of the client that triggered this event
     */
    public int getClientId() {
        return clientId;
    }

    /**
     * Returns the unique id of the client that triggered this event.
     * @return the unique id of the client that triggered this event
     */
    public String getClientUid() {
        return clientUid;
    }

    /**
     * Sets the name of the client that triggered this event.
     * @param clientName name of the client that triggered this event
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * Sets the id of the client that triggered this event.
     * @param clientId id of the client that triggered this event
     */
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    /**
     * Sets the unique id of the client that triggered this event.
     * @param clientUid unique id of the client that triggered this event
     */
    public void setClientUid(String clientUid) {
        this.clientUid = clientUid;
    }

    public String toString() {
        return String.format("Event: %s, Client: %s, ID: %d, UID: %s",
                this.getClass().getSimpleName(), getClientName(), getClientId(),
                getClientUid());
    }

    /**
     * Calls the appropriate method in the specified event listener. TS3Event
     * subclasses must implement this method in order for the events to be
     * processed by event listeners.
     *
     * @param listener event listener to receive this event
     */
    abstract public void execute(TS3EventListener listener);
}
