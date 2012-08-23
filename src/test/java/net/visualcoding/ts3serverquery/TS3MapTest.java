package net.visualcoding.ts3serverquery;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;
import java.util.Map;

/**
 * Unit test for TS3Map
 */
public class TS3MapTest extends TestCase {
    
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TS3MapTest(String testName) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(TS3MapTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        // Create a map object
        TS3Map map = new TS3Map();

        // Test the parseMapEntry method
        Map.Entry<String, List<String>> entry = null;

        entry = map.parseMapEntry("name=value");
        assertEquals(entry.getKey(), "name");
        assertEquals(entry.getValue().size(), 1);
        assertTrue(entry.getValue().contains("value"));

        entry = map.parseMapEntry("entry=val1|entry=val2|entry=val3|entry=val4");
        assertEquals(entry.getKey(), "entry");
        assertEquals(entry.getValue().size(), 4);
        String[] values = {"val1", "val2", "val3", "val4"};
        for(String value : values) assertTrue(entry.getValue().contains(value));

        // Test the parseMap method
        Map<String, List<String>> m = null;

        m = map.parseMap("name=value entry=val1|entry=val2|entry=val3");
        assertEquals(m.size(), 2);
        assertTrue(m.containsKey("name"));
        assertTrue(m.containsKey("entry"));

        assertTrue( true );
    }

}
