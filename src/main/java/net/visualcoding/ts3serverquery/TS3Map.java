package net.visualcoding.ts3serverquery;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.AbstractMap;
import java.util.HashMap;

/**
 * This class provides a map structure suitable for TS3 Server Query queries.
 *
 * @author Aldehir Rojas
 * @version 1.0.0
 */
public class TS3Map {

    /** Mapping of characters to their escaped versions. */
    private static final Map<Integer, Integer> ESCAPE_CHARS;

    /** Mapping of escaped characters to the actual charater. */
    private static final Map<Integer, Integer> UNESCAPE_CHARS;

    // Initialization. Since the methods are all static, there is no
    // instantiation of an object to perform the initialization. Instead,
    // we perform it here and initialize the character maps.
    static {
        // Instantiate maps
        ESCAPE_CHARS = new HashMap<Integer, Integer>(17);
        UNESCAPE_CHARS = new HashMap<Integer, Integer>(17);

        // Insert mappings to ESCAPE_CHARS
        ESCAPE_CHARS.put(92, 92);
        ESCAPE_CHARS.put(47, 47);
        ESCAPE_CHARS.put(32, 115);
        ESCAPE_CHARS.put(124, 112);
        ESCAPE_CHARS.put(7, 97);
        ESCAPE_CHARS.put(8, 98);
        ESCAPE_CHARS.put(12, 102);
        ESCAPE_CHARS.put(10, 110);
        ESCAPE_CHARS.put(13, 114);
        ESCAPE_CHARS.put(9, 116);
        ESCAPE_CHARS.put(11, 118);

        // Add a reflected mapping to UNESCAPE_CHARS
        for(Map.Entry<Integer, Integer> entry : ESCAPE_CHARS.entrySet()) {
            UNESCAPE_CHARS.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Map containing the mapping of string keys to a list of string values.
     * By using a list, we are able to represent keys which contain multiple
     * values that may be sent through a TS3 Server Query command, such as:
     * <pre>
     * {@code
     * client_id=1|client_id=2|client_id=3
     * }
     * </pre>
     * <p>
     * Keys associated with a null value or an empty list is interpretted as
     * a switch.
     */
    private Map<String, List<String>> map;

    /**
     * Constructs an empty map.
     */
    public TS3Map() {
        // Instantiate the map object to contain values
        map = new HashMap<String, List<String>>();
    }

    /**
     * Constructs a map and initializes it to the map representation of
     * {@code mapString}.
     *
     * @param mapString String representation of the TS3Map.
     */
    public TS3Map(String mapString) {
        // Parse string
        map = parseMap(mapString);
    }

    /**
     * Returns the {@code String} object to which the specified key is mapped,
     * or null if this map contains no mapping for the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the {@code String} object to which the specified key is mapped,
     *         or null if this map contains no mapping for the key
     */
    public String get(String key) {
        List<String> list = map.get(key);

        if(list == null || list.isEmpty()) return null;
        return ((LinkedList<String>)list).getFirst();
    }

    /**
     * Returns the {@code Integer} object to which the specified key is mapped,
     * or null if this map contains no mapping for the key or if the value
     * associated with the key is not a parsable integer.
     *
     * @param key the key whose associated value is to be returned
     * @return the {@code Integer} object to which the specified key is mapped,
     *         or null if this map contains no mapping for the key or if the
     *         value associated with the key is not a parsable integer
     */
    public Integer getInteger(String key) {
        String strValue = get(key);
        if(strValue == null) return null;

        Integer integer = null;
        try {
            integer = Integer.valueOf(strValue);
        } catch(NumberFormatException e) {
            return null;
        }

        return integer;
    }


    /**
     * Returns the {@code List<String>} object to which the specified key is
     * mapped, or null if this map contains no mapping for the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the {@code List<String>} object to which the specified key is
     *         mapped, or null if this map contains no mapping for the key
     */
    public List<String> getList(String key) {
        if(!contains(key)) return null;
        return new LinkedList<String>(map.get(key));
    }

    /**
     * Returns the list to which the specified key is mapped where the elements
     * are parsed to an Integer object, or null if this map contains no mapping
     * for the key. Elements that are not parsable integers are not included
     * in the list.
     *
     * @param key the key whose associated value is to be returned
     * @return the list to which the specified key is mapped where the elements
     *         are parsed to an Integer object, or null if this map contains no
     *         mapping for the key
     */
    public List<Integer> getIntegerList(String key) {
        if(!contains(key)) return null;

        // Create a list of integers
        List<Integer> list = new LinkedList<Integer>();
        List<String> stringList = map.get(key);

        if(stringList == null) return null;

        for(String item : stringList) {
            try {
                list.add(Integer.valueOf(item));
            } catch(NumberFormatException e) {
                // Ignore
            }
        }

        // Return null if list is empty
        if(list.isEmpty()) return null;

        // Otherwise return the list
        return list;
    }

    /**
     * Returns {@code true} if this map contains {@code key} and the value of
     * {@code key} is a list.
     *
     * @param key key to check to see if associated value is a list
     * @return {@code true} if this map contains {@code key} and the value of
     *         {@code key} is a list
     */
    public boolean isList(String key) {
        List<String> list = map.get(key);

        // Only consider it a list if it has more than 1 item
        if(list == null || list.size() <= 1) return false;
        return true;
    }

    /**
     * Returns {@code true} if this map contains {@code key} and the value of
     * {@code key} is an integer.
     *
     * @param key key to check to see if associated value is an integer
     * @return {@code true} if this map contains {@code key} and the value of
     *         {@code key} is an integer
     */
    public boolean isInteger(String key) {
        String value = get(key);
        if(value == null) return false;

        try {
            Integer.parseInt(value);
        } catch(NumberFormatException e) {
            // If we can't parse it, then it must not be an integer
            return false;
        }

        return true;
    }

    /**
     * Returns {@code true} if this map contains {@code key} and {@code key} is
     * a switch.
     *
     * @param key key to check if associated value is a switch
     * @return {@code true} if this map contains {@code key} and {@code key} is
     *         a switch
     */
    public boolean isSwitch(String key) {
        return contains(key) && get(key) == null;
    }

    /**
     * Returns {@code true} if this map is empty.
     * @return {@code true} if this map is empty
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns the size of this map.
     * @return the size of this map
     */
    public int size() {
        return map.size();
    }

    /**
     * Associates the specified value with the specified key in this map. If
     * the map already contains the specified key, then the specified value is
     * appended to the list associated with the specified key.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return {@code true} if the mapping was successfully added
     */
    public boolean add(String key, String value) {
        // Trim key/values
        key = key.trim();
        value = value.trim();

        // Don't add if the key or value variables are empty
        if(key.isEmpty() || value.isEmpty()) return false;

        // Get the list of the given key
        List<String> list = map.get(key);

        // If key doesn't exist, then instantiate a list and
        // add it under the key
        if(list == null) {
            list = new LinkedList<String>();
            map.put(key, list);
        }

        // Add value to the list
        list.add(value);

        return true;
    }

    /**
     * Associates the specified integer value with the specified key in this
     * map. If the map already contains the specified key, then the specified
     * value is append to the list associated with the specified key.
     *
     * @param key     key with which the sepcified value is to be associated
     * @param integer integer to be assocaited with the specified key
     * @return {@code true} if the mapping was successfully added
     */
    public boolean add(String key, int integer) {
        return add(key, Integer.toString(integer));
    }

    /**
     * Associates the specified key as a switch in this map.
     *
     * @param key key which is to be associated with a switch
     * @return {@code true} if the mapping was successfully added,
     *         {@code false} if the map already contains the specified key
     *         and is not a switch.
     */
    public boolean add(String key) {
        List<String> value = map.get(key);

        // If there is no mapping, or if the value is null then
        // add to the map and return true
        if(value == null) {
            map.put(key, null);
            return true;
        }

        // Otherwise return false
        return false;
    }

    /**
     * Returns {@code true} if this map contains {@code key}.
     * @param key key to check whether or not it exists in this map
     * @return {@code true} if this map contains {@code key}
     */
    public boolean contains(String key) {
        return map.containsKey(key);
    }

    /**
     * Removes the mapping of the specified key in this map.
     * @param key key to remove from this map
     * @return {@code true} if the key exists and was removed
     */
    public boolean remove(String key) {
        // Return false if there is no mapping
        if(!map.containsKey(key)) return false;

        // Otherwise, remove the mapping and return true
        map.remove(key);
        return true;
    }

    /**
     * Removes all the mappings in this map.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Parses the specified string representation of a TS3Map.
     *
     * @param mapString String representation of a TS3Map
     * @return Internal map object used by TS3Map containing the mappings in the
     *         specified string.
     */
    protected Map<String, List<String>> parseMap(String mapString) {
        // Instantiate a map
        Map<String, List<String>> map = new HashMap<String, List<String>>();

        // Split by whitespace
        String[] entries = mapString.split("\\s+");

        for(String item : entries) {
            // Parse entry
            Map.Entry<String, List<String>> entry = parseMapEntry(item);

            // Add to the map
            map.put(entry.getKey(), entry.getValue());
        }

        return map;
    }

    /**
     * Parses the specified string representation of an entry in a TS3Map.
     *
     * @param entry String representation of an entry for TS3Map
     * @return A Map.Entry object containing the parsed contents of the
     *         specified string.
     */
    protected Map.Entry<String, List<String>> parseMapEntry(String entry) {
        // Trim whitespace and return null if empty
        entry = entry.trim();
        if(entry.isEmpty()) return null;

        // Check if this is a switch
        if(entry.startsWith("-")) {
            // Get the rest of the string
            String switchName = entry.substring(1);

            // Return a map entry with the value as null, implying a switch
            return new AbstractMap.SimpleEntry(switchName, null);
        }

        String name = null; // Name
        List<String> pairs = new LinkedList<String>();  // List of pairs
        List<String> values = new LinkedList<String>(); // List of values

        // If there is a pipe character, then add all the pairs to the list
        if(entry.indexOf('|') != -1) {
            String[] subEntries = entry.split("\\|");
            for(String subEntry : subEntries) {
                if(!subEntry.isEmpty()) pairs.add(subEntry);
            }
        } else {
            pairs.add(entry);
        }

        // Loop through all of the pairs
        for(String pair : pairs) {
            // Skip if there is not an equal sign
            if(pair.indexOf('=') == -1) continue;

            // Split by equal sign
            String[] nameValue = pair.split("=");

            // Set the name of the entry
            if(name == null) name = nameValue[0];

            // Unescape the value and add to the list of values
            values.add(unescape(nameValue[1]));
        }

        // Return map entry
        return new AbstractMap.SimpleEntry<String, List<String>>(name, values);
    }

    /**
     * Returns a string representation of a Map.Entry object.
     * @param entry entry to represent as a string
     * @return a string representation of a Map.Entry object.
     */
    protected String entryToString(Map.Entry<String, List<String>> entry) {
        StringBuilder sb = new StringBuilder();

        if(entry.getValue() == null) {
            // Handle switches
            sb.append('-');
            sb.append(entry.getKey());

            return sb.toString();
        }

        // Get the list
        List<String> list = entry.getValue();

        // Iterate through the values and add each to our string
        Iterator<String> it = list.iterator();
        while(it.hasNext()) {
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(escape(it.next()));

            // Add a pipe if there are more values
            if(it.hasNext()) sb.append('|');
        }

        return sb.toString();
    }

    /**
     * Returns a string representation of this map.
     * @return a string representation of this map
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Get an iterator for our map
        Iterator<Map.Entry<String, List<String>>> it;
        it = map.entrySet().iterator();

        while(it.hasNext()) {
            sb.append(entryToString(it.next()));

            // Add space if there are more entries
            if(it.hasNext()) sb.append(' ');
        }

        return sb.toString();
    }

    /**
     * Escape {@code str} using the TS3 Server Query escape characters.
     * @param str String to escape
     * @return Escaped string
     */
    public static String escape(String str) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < str.length(); ++i) {
            // Get the character and it's integer object
            char character = str.charAt(i);
            Integer intval = Integer.valueOf((int)character);

            // Check if we need to escape this character
            if(ESCAPE_CHARS.containsKey(intval)) {
                // Append a slash and the character
                sb.append('\\');
                sb.append((char)ESCAPE_CHARS.get(intval).intValue());
            } else {
                // Add character
                sb.append(character);
            }
        }

        return sb.toString();
    }

    /**
     * Unescape {@code str} using the TS3 Server Query escape characters.
     * @param str String to unescape
     * @return Unescaped string
     */
    public static String unescape(String str) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < str.length(); ++i) {
            // Get the character
            char character = str.charAt(i);

            // Check if this is a backslash
            if(character == '\\') {

                // Ignore if the backspace isn't followed by at least
                // one more character
                if(i < str.length() - 1) {
                    // Move and get the next character
                    character = str.charAt(++i);
                    Integer intval = Integer.valueOf((int)character);

                    // Append the character
                    if(UNESCAPE_CHARS.containsKey(intval)) {
                        sb.append((char)UNESCAPE_CHARS.get(intval).intValue());
                    }
                }

            } else {
                // Add in character
                sb.append(character);
            }
        }

        return sb.toString();
    }
}
