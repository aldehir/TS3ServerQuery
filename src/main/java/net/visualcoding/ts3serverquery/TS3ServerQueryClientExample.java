package net.visualcoding.ts3serverquery;

import net.visualcoding.ts3serverquery.event.TS3ClientConnectedEvent;
import net.visualcoding.ts3serverquery.event.TS3ClientDisconnectedEvent;
import net.visualcoding.ts3serverquery.event.TS3ClientMovedEvent;
import net.visualcoding.ts3serverquery.event.TS3MessageEvent;

public class TS3ServerQueryClientExample {
    
    public static void main(String[] args) throws Exception {
        TS3ServerQueryClient q = new TS3ServerQueryClient("localhost");
        q.connect();

        // Command: login serveradmin UyN35cJO
        TS3Result result = q.execute("login serveradmin UyN35cJO");
        System.out.println(result);

        // Command: use sid=1
        TS3Command use = (new TS3Command("use")).add("sid", 1);
        result = q.execute(use);
        System.out.println(result);

        // Register all notifications
        if(q.registerNotifications()) {
            System.out.println("Registered for notifications");
        }

        // Create an event listener
        TS3EventListener listener = new TS3EventListener() {
            public void onClientConnected(TS3ClientConnectedEvent event) {
                System.out.println("Client connected: " + event.getClientName());
            }

            public void onClientDisconnected(TS3ClientDisconnectedEvent event) {
                System.out.println("Client disconnected: " + event.getClientName());
            }

            public void onClientMoved(TS3ClientMovedEvent event) {
                System.out.println("Client moved: " + event.getClientName() +
                        " from " + event.getSource() + " to " + event.getDestination());
            }

            public void onMessage(TS3MessageEvent event) {
                System.out.println("Message received from: " + event.getClientName());
            }
        };

        // Add listener
        q.addEventListener(listener);


        // Spawn other threads to execute commands (and see how it works with our polling thread)
        /*
        while(true) {
            Thread t = new Thread(new RunnableTest(q));
            t.start();

            try { Thread.sleep(20); }
            catch(Exception e) { }
        }
        */
    }

    private static class RunnableTest implements Runnable {
        private TS3ServerQueryClient serverQuery;

        public RunnableTest(TS3ServerQueryClient serverQuery) {
            this.serverQuery = serverQuery;
        }

        public void run() {
            try {
                // Execute a command... like "clientlist"
                TS3Result result = serverQuery.execute("clientlist");
                if(!result.hasError()) {
                    System.out.println("Thread executed");
                }
            } catch(Exception e) { }
        }
    }
}
