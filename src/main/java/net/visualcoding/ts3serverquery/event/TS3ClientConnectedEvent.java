package net.visualcoding.ts3serverquery.event;

import net.visualcoding.ts3serverquery.TS3EventListener;

public class TS3ClientConnectedEvent extends TS3Event {
    public TS3ClientConnectedEvent(String clientName, int clientId, String clientUid) {
        super(clientName, clientId, clientUid);
    }

    public void execute(TS3EventListener listener) {
        listener.onClientConnected(this);
    }
}
