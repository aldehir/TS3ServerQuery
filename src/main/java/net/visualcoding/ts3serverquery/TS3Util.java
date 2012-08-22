package net.visualcoding.ts3serverquery;

import java.lang.StringBuilder;
import java.util.Map;
import java.util.HashMap;

public class TS3Util {

    private static final Map<Integer, Integer> escapeChars;
    private static final Map<Integer, Integer> unescapeChars;

    static {
        // Instantiate maps
        escapeChars = new HashMap<Integer, Integer>(17);
        unescapeChars = new HashMap<Integer, Integer>(17);

        // Insert mappings to escapeChars
        escapeChars.put(92, 92);
        escapeChars.put(47, 47);
        escapeChars.put(32, 115);
        escapeChars.put(124, 112);
        escapeChars.put(7, 97);
        escapeChars.put(8, 98);
        escapeChars.put(12, 102);
        escapeChars.put(10, 110);
        escapeChars.put(13, 114);
        escapeChars.put(9, 116);
        escapeChars.put(11, 118);

        // Add a reflected mapping to unescapeChars
        for(Map.Entry<Integer, Integer> entry : escapeChars.entrySet()) {
            unescapeChars.put(entry.getValue(), entry.getKey());
        }
    }

    public static String escape(String str) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < str.length(); ++i) {
            // Get the character and it's integer object
            char character = str.charAt(i);
            Integer intval = Integer.valueOf((int)character);

            // Check if we need to escape this character
            if(escapeChars.containsKey(intval)) {
                // Append a slash and the character
                sb.append('\\');
                sb.append((char)escapeChars.get(intval).intValue());
            } else {
                // Add character
                sb.append(character);
            }
        }

        return sb.toString();
    }

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
                    if(unescapeChars.containsKey(intval)) {
                        sb.append((char)unescapeChars.get(intval).intValue());
                    }
                }

            } else {
                // Add in character
                sb.append(character);
            }
        }

        return sb.toString();
    }

    public static Map<String, String> parseDetails(String line) {
        // Create a map
        Map<String, String> map = new HashMap<String, String>(13);

        // Parse by whitespace
        String[] parts = line.split("\\s+");

        for(String part : parts) {
            // Skip if there is no equal sign (for whatever reason)
            if(part.indexOf('=') == -1) continue;

            // Split by the equal sign, limit to only 2
            String[] pairs = part.split("=", 2);

            // Add pair to our map
            map.put(pairs[0], TS3Util.unescape(pairs[1]));
        }

        return map;
    }

}
