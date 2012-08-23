package net.visualcoding.ts3serverquery;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;

public class TS3Map {

    Map<String, List<String>> map;
    
    public TS3Map() {
        // Instantiate the map object to contain values
        map = new HashMap<String, List<String>>();
    }

    public TS3Map(String mapString) {
        // Parse string
        map = parseMap(mapString);
    }

    public boolean add(String name, String value) {
        // Trim name/values
        name = name.trim();
        value = value.trim();

        // Don't add if the name or value variables are empty
        if(name.isEmpty() || value.isEmpty()) return false;
        
        // Get the list of the given key/name
        List<String> list = map.get(name);

        // If key doesn't exist, then instantiate a list and add it under the
        // key
        if(list == null) {
            list = new List<String>();
            map.put(name, list);
        }

        // Add value to the list
        list.add(value);

        return true;
    }

    public boolean addInteger(String name, int integer) {
        return add(name, Integer.toString(integer));
    }

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

    protected Map.Entry<String, List<String>> parseMapEntry(String entry) {
        // Trim whitespace and return null if empty
        entry = entry.trim();
        if(entry.isEmpty()) return null;

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
            values.add(TS3Util.unescape(nameValue[1]));
        }

        // Return map entry
        return new AbstractMap.SimpleEntry<String, List<String>>(name, values);
    }
}
