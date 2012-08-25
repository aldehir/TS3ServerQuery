package net.visualcoding.ts3serverquery;

import net.visualcoding.ts3serverquery.event.*;

import org.junit.*;
import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;
import java.io.IOException;

/**
 * Unit test for TS3ServerQuery
 */
public class TS3ServerQueryClientTest {

    /*
    @Test(expected=IOException.class)
    public void testFailedConnection() throws IOException {
        TS3ServerQueryClient client = new TS3ServerQueryClient("unkownhost", 5123);
        client.connect();
    }
    */

    @Test(timeout=10000)
    public void testServerQueryClient() {

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

            // Wait until all events are processed before terminating
            assertTrue(server.semEvents.tryAcquire(10, TimeUnit.SECONDS));

            // Wait until all messages are processed before terminating
            assertTrue(server.semMessages.tryAcquire(10, TimeUnit.SECONDS));

            // Disconnect from the server
            client.disconnect();
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }

        assertTrue( true );
    }

    private class EventListener implements TS3EventListener {
        TS3ServerDummy server;

        public EventListener(TS3ServerDummy server) {
            this.server = server;
        }

        public void onClientMoved(TS3ClientMovedEvent event) {
            // Check the server if the client is in the channel
            assertTrue(server.isClientInChannel(event.getClientId(),
                    event.getDestination()));

            signalEvent();
        }

        public void onClientConnected(TS3ClientConnectedEvent event) {
            // Check that the server contains the client
            assertTrue(server.hasClient(event.getClientId()));

            signalEvent();
        }

        public void onClientDisconnected(TS3ClientDisconnectedEvent event) {
            // Check that the server does not contain the client
            assertFalse(server.hasClient(event.getClientId()));

            signalEvent();
        }

        public void onMessage(TS3MessageEvent event) {
            server.semMsgReceived.release();
        }

        protected void signalEvent() {
            server.semEventReceived.release();
        }
    }

}
