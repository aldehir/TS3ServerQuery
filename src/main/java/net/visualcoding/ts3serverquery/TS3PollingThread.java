package net.visualcoding.ts3serverquery;

import net.visualcoding.ts3serverquery.event.*;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

public class TS3PollingThread extends Thread {
    private static final int LATENCY = 500;

    private TS3ServerQueryClient serverQuery;
    private Map<Integer, ClientInfo> map = null;

    public TS3PollingThread(TS3ServerQueryClient serverQuery) {
        this.serverQuery = serverQuery;

        map = new HashMap<Integer, ClientInfo>(151);
    }

    public void run() {
        while(true) {
            TS3Result result = null;
            
            try {
                // Get all the clients connected
                result = serverQuery.execute("clientlist -uid");
            } catch(Exception e) { continue; }

            // Create a hash map of the currently connected clients
            HashMap<Integer, Map<String, String>> currentClients = new HashMap<Integer, Map<String, String>>(151);

            // Add results to our hash map of clients
            for(Map<String, String> item : result.getItems()) {
                try {
                    int clientId = Integer.parseInt(item.get("clid"));
                    currentClients.put(clientId, item);
                } catch(Exception e) { }
            }

            System.out.println(currentClients);

            // Send disconnected events and delete disconnected clients from our map
            Iterator<Map.Entry<Integer, ClientInfo>> mapIt = map.entrySet().iterator();
            while(mapIt.hasNext()) {
                Map.Entry<Integer, ClientInfo> current = mapIt.next();

                if(!currentClients.containsKey(current.getKey())) {
                    ClientInfo info = current.getValue();
                    mapIt.remove();

                    serverQuery.notify(new TS3ClientDisconnectedEvent(
                        info.clientName, info.clientId, info.clientUid
                    ));
                }
            }

            // Send connected events, add clients to our map and delete them from currentClients
            Iterator<Map.Entry<Integer, Map<String, String>>> clientsIt = currentClients.entrySet().iterator();
            while(clientsIt.hasNext()) {
                Map.Entry<Integer, Map<String, String>> item = clientsIt.next();

                if(!map.containsKey(item.getKey())) {
                    clientsIt.remove();

                    try {
                        ClientInfo info = new ClientInfo(
                            item.getValue().get("client_nickname"),
                            item.getKey().intValue(),
                            item.getValue().get("client_unique_identifier"),
                            Integer.parseInt(item.getValue().get("cid"))
                        );

                        map.put(item.getKey(), info);

                        serverQuery.notify(new TS3ClientConnectedEvent(
                            info.clientName, info.clientId, info.clientUid
                        ));
                    } catch(Exception e) { }
                }
            }

            // Send moved events
            clientsIt = currentClients.entrySet().iterator();
            while(clientsIt.hasNext()) {
                Map.Entry<Integer, Map<String, String>> item = clientsIt.next();

                if(map.containsKey(item.getKey())) {
                    // Get the info from the map
                    ClientInfo info = map.get(item.getKey());

                    // Set name
                    info.clientName = item.getValue().get("client_nickname");

                    try {
                        int currentChannel = Integer.parseInt(item.getValue().get("cid"));

                        // If the channels are different, then send event
                        if(currentChannel != info.channelId) {
                            // Create event
                            TS3ClientMovedEvent event = new TS3ClientMovedEvent(
                                info.clientName, info.clientId, info.clientUid
                            );

                            event.setSource(info.channelId);
                            event.setDestination(currentChannel);

                            // Send
                            serverQuery.notify(event);

                            // Update channel
                            info.channelId = currentChannel;
                        }
                    } catch(Exception e) { }
                }
            }

            // Wait a bit before polling again
            try { Thread.sleep(LATENCY); }
            catch(Exception e) { /* ... */ }
        }
    }

    static private class ClientInfo {
        public String clientName;
        public int clientId;
        public String clientUid;
        public int channelId;

        public ClientInfo(String clientName, int clientId, String clientUid, int channelId) {
            this.clientName = clientName;
            this.clientId = clientId;
            this.clientUid = clientUid;
            this.channelId = channelId;
        }
    }
}
