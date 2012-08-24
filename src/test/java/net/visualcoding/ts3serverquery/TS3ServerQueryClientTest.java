package net.visualcoding.ts3serverquery;

import net.visualcoding.ts3serverquery.event.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.TimeUnit;

/**
 * Unit test for TS3ServerQuery
 */
public class TS3ServerQueryClientTest extends TestCase {
    
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TS3ServerQueryClientTest(String testName) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TS3ServerQueryClientTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        final int port = 14512;
        
        // Create a server dummy, execute on it's own thread
        TS3ServerDummy server = new TS3ServerDummy(port);
        server.start();
        
        try {
            // Let the server start up before trying to connect
            Thread.sleep(200);
            
            // Create our server query client
            TS3ServerQueryClient client = new TS3ServerQueryClient("localhost", port);
            client.connect();
            
            // Log in
            //client.execute("login user pass");
            TS3Map map = new TS3Map();
            map.add("client_login_name", "user");
            map.add("client_login_password", "password");

            client.execute("login", map);

            // Add an event listener
            client.addEventListener(new EventListener(server));

            // Register notifications with polling
            client.registerNotifications(true);

            // Assert that these events were registered on the server
            String[] events = {"textserver", "textchannel", "textprivate"};
            for(String event : events) {
                assertTrue(server.isEventRegistered(event));
            }

            // Wait until all events are processed
            assertTrue(server.semEvents.tryAcquire(5000, TimeUnit.MILLISECONDS));
        } catch(Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
                
        assertTrue( true );
    }

    private class EventListener implements TS3EventListener {
        TS3ServerDummy server;

        public EventListener(TS3ServerDummy server) {
            this.server = server;
        }

        public void onClientMoved(TS3ClientMovedEvent event) {
            System.out.println("Client moved: " + event.getClientName());

            // Check the server if the client is in the channel
            assertTrue(server.isClientInChannel(event.getClientId(),
                    event.getDestination()));
            
            signalEvent();
        }

        public void onClientConnected(TS3ClientConnectedEvent event) {
            System.out.println("Client connected: " + event.getClientName());

            // Check that the server contains the client
            assertTrue(server.hasClient(event.getClientId()));

            signalEvent();
        }

        public void onClientDisconnected(TS3ClientDisconnectedEvent event) {
            System.out.println("Client disconnected: " + event.getClientName());

            // Check that the server does not contain the client
            assertFalse(server.hasClient(event.getClientId()));

            signalEvent();
        }

        public void onMessage(TS3MessageEvent event) {

        }

        protected void signalEvent() {
            server.semEventReceived.release();
        }
    }

}
