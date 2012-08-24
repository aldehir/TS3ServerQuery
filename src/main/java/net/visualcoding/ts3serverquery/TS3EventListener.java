package net.visualcoding.ts3serverquery;

import net.visualcoding.ts3serverquery.event.TS3ClientMovedEvent;
import net.visualcoding.ts3serverquery.event.TS3ClientConnectedEvent;
import net.visualcoding.ts3serverquery.event.TS3ClientDisconnectedEvent;
import net.visualcoding.ts3serverquery.event.TS3MessageEvent;

public interface TS3EventListener {
    public void onClientMoved(TS3ClientMovedEvent event);
    public void onClientConnected(TS3ClientConnectedEvent event);
    public void onClientDisconnected(TS3ClientDisconnectedEvent event);
    public void onMessage(TS3MessageEvent event);
}
