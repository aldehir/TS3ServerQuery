package net.visualcoding.ts3serverquery.event;

import net.visualcoding.ts3serverquery.TS3EventListener;

abstract public class TS3Event {
    private String clientName;
    private int clientId;
    private String clientUid;

    public TS3Event(String clientName, int clientId, String clientUid) {
        setClientName(clientName);
        setClientId(clientId);
        setClientUid(clientUid);
    }

    public String getClientName() {
        return clientName;
    }

    public int getClientId() {
        return clientId;
    }

    public String getClientUid() {
        return clientUid;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public void setClientUid(String clientUid) {
        this.clientUid = clientUid;
    }

    abstract public void execute(TS3EventListener listener);
}
