package net.visualcoding.ts3serverquery;

import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class TS3Result {

    private ArrayList<Map<String, String>> items;
    private int errorCode;
    private String errorMessage;

    public TS3Result(String[] result) {
        // Instantiate the array list to hold items
        items = new ArrayList<Map<String, String>>();

        // parse
        parseResultArray(result);
    }

    public Map<String, String> getFirst() {
        if(!items.isEmpty()) return items.get(0);
        return null;
    }

    public ArrayList<Map<String, String>> getItems() {
        return items;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean hasError() {
        return errorCode != 0;
    }

    private void parseResultArray(String[] result) {
        // Loop through the first n-1 items
        for(int i = 0; i < result.length - 1; i++) {
            String line = result[i];

            // Split by pipe character
            String[] split = line.split("\\|");

            for(int j = 0; j < split.length; j++) {
                String trimmed = split[j].trim();

                // Parse and add to our items
                if(!trimmed.isEmpty()) {
                    items.add(TS3Util.parseDetails(trimmed));
                }
            }
        }

        // Parse and set the error code/message
        String[] err = result[result.length - 1].split("\\s+", 2);
        Map<String, String> errorMap = TS3Util.parseDetails(err[1]);

        errorMessage = errorMap.get("msg");

        try {
            errorCode =  Integer.parseInt(errorMap.get("id"));
        } catch(Exception e) {
            errorCode = -1;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Error Code: ");
        sb.append(getErrorCode());
        sb.append("\nError Message: ");
        sb.append(getErrorMessage());

        if(items.size() > 0) {
            sb.append("\n");

            for(Map<String, String> item : items) {
                sb.append(item.toString());
                sb.append("\n");
            }
        }

        return sb.toString();
    }

}
