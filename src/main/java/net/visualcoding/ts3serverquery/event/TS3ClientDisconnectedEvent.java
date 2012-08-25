package net.visualcoding.ts3serverquery.event;

import net.visualcoding.ts3serverquery.TS3EventListener;

/**
 * This event is triggered when a client disconnects from the Teamspeak 3
 * server.
 *
 * @author Aldehir Rojas
 * @version 1.0
 */
public class TS3ClientDisconnectedEvent extends TS3Event {

    /**
     * Constructs a client disconnected event with the specified client details.
     *
     * @param clientName name of the client that triggered this event
     * @param clientId   id of the client that triggered this event
     * @param clientUid  unique id of the client that triggered this event
     */
    public TS3ClientDisconnectedEvent(String clientName, int clientId,
            String clientUid) {
        super(clientName, clientId, clientUid);
    }

    /**
     * Calls {@link TS3EventListener#onClientDisconnected()}, passing this event
     */
    public void execute(TS3EventListener listener) {
        listener.onClientDisconnected(this);
    }

}
