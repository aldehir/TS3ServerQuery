package net.visualcoding.ts3serverquery;

import net.visualcoding.ts3serverquery.event.*;

public interface TS3EventListener {
    public void onClientMoved(TS3ClientMovedEvent event);
    public void onClientConnected(TS3ClientConnectedEvent event);
    public void onClientDisconnected(TS3ClientDisconnectedEvent event);
    public void onMessage(TS3MessageEvent event);
}
