package net.visualcoding.ts3serverquery.event;

import net.visualcoding.ts3serverquery.TS3EventListener;

/**
 * This event is triggered when a client in the Teamspeak 3 server moves to
 * another channel.
 *
 * @author Aldehir Rojas
 * @version 1.0
 */
public class TS3ClientMovedEvent extends TS3Event {

    /** Channel that the client moved from. */
    private int src;

    /** Channel that the client moved to. */
    private int dest;

    /**
     * Constructs a client moved event with all string fields initialized to
     * {@code null} and integer fields initialized to 0.
     */
    public TS3ClientMovedEvent() {
        this(null, 0, null, 0, 0);
    }

    /**
     * Constructs a client moved event with the specified client details.
     *
     * @param clientName name of the client that triggered this event
     * @param clientId   id of the client that triggered this event
     * @param clientUid  unique id of the client that triggered this event
     */
    public TS3ClientMovedEvent(String clientName, int clientId,
            String clientUid) {
        super(clientName, clientId, clientUid);
    }

    /**
     * Constructs a client moved event with the specified client details,
     * and the specified source and destination channels.
     *
     * @param clientName  name of the client that triggered this event
     * @param clientId    id of the client that triggered this event
     * @param clientUid   unique id of the client that triggered this event
     * @param source      channel that the client moved from
     * @param destination channel that the client moved to
     */
    public TS3ClientMovedEvent(String clientName, int clientId,
            String clientUid, int source, int destination) {
        super(clientName, clientId, clientUid);
        setSource(source);
        setDestination(destination);
    }

    /**
     * Retuns the source channel. In other words, this returns the channel that
     * the client moved from.
     *
     * @return the source channel
     */
    public int getSource() {
        return src;
    }

    /**
     * Returns the destination channel. In other words, this returns the channel
     * that the client moved to.
     *
     * @return the destination channel
     */
    public int getDestination() {
        return dest;
    }

    /**
     * Sets the source channel.
     * @param src the source channel
     */
    public void setSource(int src) {
        this.src = src;
    }

    /**
     * Sets the destination channel.
     * @param dest the destination channel
     */
    public void setDestination(int dest) {
        this.dest = dest;
    }

    /**
     * Calls {@link TS3EventListener#onClientMoved()} and passes this event
     */
    public void execute(TS3EventListener listener) {
        listener.onClientMoved(this);
    }

}
