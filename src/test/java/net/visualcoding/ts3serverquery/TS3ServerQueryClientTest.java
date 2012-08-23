package net.visualcoding.ts3serverquery;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
            Thread.sleep(500);
            
            // Create our server query client
            TS3ServerQueryClient client = new TS3ServerQueryClient("localhost", port);
            client.connect();
            
            // Register notifications
            
            // Log in
            client.execute("login user pass");
        } catch(Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
                
        assertTrue( true );
    }
}
