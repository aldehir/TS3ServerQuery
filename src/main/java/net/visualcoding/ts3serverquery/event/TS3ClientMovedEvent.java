package net.visualcoding.ts3serverquery.event;

import net.visualcoding.ts3serverquery.TS3EventListener;

public class TS3ClientMovedEvent extends TS3Event {
    private int src;
    private int dest;

    public TS3ClientMovedEvent(String clientName, int clientId, String clientUid) {
        super(clientName, clientId, clientUid);
    }

    public int getSource() {
        return src;
    }

    public int getDestination() {
        return dest;
    }

    public void setSource(int src) {
        this.src = src;
    }

    public void setDestination(int dest) {
        this.dest = dest;
    }

    public void execute(TS3EventListener listener) {
        listener.onClientMoved(this);
    }
}
