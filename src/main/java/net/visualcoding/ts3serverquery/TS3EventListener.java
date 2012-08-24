package net.visualcoding.ts3serverquery;

import net.visualcoding.ts3serverquery.event.TS3ClientMovedEvent;
import net.visualcoding.ts3serverquery.event.TS3ClientConnectedEvent;
import net.visualcoding.ts3serverquery.event.TS3ClientDisconnectedEvent;
import net.visualcoding.ts3serverquery.event.TS3MessageEvent;

/**
 * The {@code TS3EventListener} interface provides the necessary methods that
 * an event listener needs to implement in order to listen for events from
 * the TS3 Server.
 */
public interface TS3EventListener {

    /**
     * A client moved to another channel.
     * @param event Event object containing the details of the event
     */
    public void onClientMoved(TS3ClientMovedEvent event);

    /**
     * A client connected to the server.
     * @param event Event object containing the details of the event
     */
    public void onClientConnected(TS3ClientConnectedEvent event);

    /**
     * A client disconnected from the server.
     * @param event Event object containing the details of the event
     */
    public void onClientDisconnected(TS3ClientDisconnectedEvent event);

    /**
     * Message received from either the global server channel, the channel the
     * query client resides, or another client.
     * @param event Event object containing the details of the event
     */
    public void onMessage(TS3MessageEvent event);

}
