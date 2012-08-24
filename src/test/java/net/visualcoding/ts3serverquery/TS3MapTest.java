package net.visualcoding.ts3serverquery;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;
import java.util.Map;
import java.util.Arrays;

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

        // Test the parseMapEntry() method
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

        entry = map.parseMapEntry("-switch");
        assertEquals(entry.getKey(), "switch");
        assertNull(entry.getValue());

        // Test the parseMap() method
        Map<String, List<String>> m = null;

        m = map.parseMap("name=value entry=val1|entry=val2|entry=val3 -switch");
        assertEquals(m.size(), 3);
        assertTrue(m.containsKey("name"));
        assertTrue(m.containsKey("entry"));
        assertTrue(m.containsKey("switch"));
        assertNull(m.get("switch"));

        // Test the entryToString() method
        values = new String[] { "-switch", "name=value", "name=v1|name=v2" };
        for(String value : values) {
            entry = map.parseMapEntry(value);
            assertEquals(map.entryToString(entry), value);
        }

        // Test the toString() method
        map = new TS3Map("name=value -switch");

        // Since we're using a HashMap, the order may not be the same.
        boolean valid = false;
        if(map.toString().equals("name=value -switch")) valid = true;
        if(map.toString().equals("-switch name=value")) valid = true;
        assertTrue(valid);

        // Test the add methods
        map.clear();
        map.add("name", "value");
        assertEquals(map.toString(), "name=value");

        map.clear();
        map.add("entry", "v1");
        map.add("entry", "v2");
        map.add("entry", "v3");
        assertEquals(map.toString(), "entry=v1|entry=v2|entry=v3");

        map.clear();
        map.add("client_id", 1);
        assertEquals(map.toString(), "client_id=1");

        map.clear();
        map.add("uids");
        assertEquals(map.toString(), "-uids");

        // Test the get methods
        map.clear();

        // Ensure that the methods work properly when there is no such mapping
        assertFalse(map.contains("none"));

        assertNull(map.get("none"));
        assertNull(map.getList("none"));
        assertNull(map.getInteger("none"));
        assertNull(map.getIntegerList("none"));

        assertFalse(map.isList("none"));
        assertFalse(map.isInteger("none"));
        assertFalse(map.isSwitch("none"));

        // Test a single key with a single value
        map.add("entry", "value");
        assertTrue(map.contains("entry"));
        assertEquals(map.get("entry"), "value");
        assertNull(map.getInteger("entry"));

        assertFalse(map.isList("entry"));
        assertFalse(map.isInteger("entry"));
        assertFalse(map.isSwitch("entry"));

        // Test a single key with multiple values
        values = new String[] { "value1", "value2", "value3" };
        for(String val : values) map.add("list", val);

        assertTrue(map.isList("list"));
        assertFalse(map.isInteger("list"));
        assertFalse(map.isSwitch("list"));

        assertNull(map.getInteger("list"));
        assertEquals(map.get("list"), "value1");
        assertEquals(map.getList("list"), Arrays.asList(values));

        // Test integers
        map.add("int", "123");
        map.add("int2", 1234);
        assertTrue(map.isInteger("int"));
        assertTrue(map.isInteger("int2"));

        assertEquals(map.getInteger("int"), new Integer(123));
        assertEquals(map.getInteger("int2"), new Integer(1234));

        assertFalse(map.isList("int"));
        assertFalse(map.isSwitch("int"));

        // Test integer lists
        Integer[] intVals = new Integer[] { 1, 2, 3, 4 };
        for(Integer val : intVals) map.add("intList", val);

        assertTrue(map.isList("intList"));
        assertEquals(map.getIntegerList("intList"), Arrays.asList(intVals));

        // Test switches
        map.add("switch");
        assertTrue(map.isSwitch("switch"));
        assertFalse(map.isList("switch"));
        assertFalse(map.isInteger("switch"));
    }

}
