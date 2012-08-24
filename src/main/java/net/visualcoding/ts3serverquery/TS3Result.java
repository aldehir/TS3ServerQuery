package net.visualcoding.ts3serverquery;

import java.util.List;
import java.util.ArrayList;

/**
 * This class is a container for the TS3 Server Query responses.
 *
 * @author Aldehir Rojas
 * @version 1.0
 */
public class TS3Result {

    /** List of items represented by a {@link TS3Map}. */
    private List<TS3Map> items;

    /** Error code of the command. */
    private int errorCode;

    /** Error message of the command. */
    private String errorMessage;

    /**
     * Constructs a TS3Result object with the responses received from the
     * Teamspeak 3 Server.
     *
     * @param result Array of responses received from the Teamspeak 3 Server
     */
    public TS3Result(String[] result) {
        // Instantiate the array list to hold items
        items = new ArrayList<TS3Map>();

        // parse
        parseResultArray(result);
    }

    /**
     * Returns the first item in the result list.
     * @return the first item in the result list
     */
    public TS3Map getFirst() {
        if(!items.isEmpty()) return items.get(0);
        return null;
    }

    /**
     * Returns the list of result items.
     * @return the list of result items
     */
    public List<TS3Map> getItems() {
        return items;
    }

    /**
     * Returns the error code of the result.
     * @return the error code of the result
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the error message of the result.
     * @return the error message of the result
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns {@code true} if the result is an error result; in order words,
     * when the error code is non-zero.
     *
     * @return {@code true} if the result has an non-zero error code
     */
    public boolean hasError() {
        return errorCode != 0;
    }

    /**
     * Parses the Teamspeak 3 Server response.
     * @param result Teamspeak 3 Server response
     */
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
                    items.add(new TS3Map(trimmed));
                }
            }
        }

        // Parse and set the error code/message
        String[] err = result[result.length - 1].split("\\s+", 2);
        TS3Map errorMap = new TS3Map(err[1]);

        errorMessage = errorMap.get("msg");
        errorCode = errorMap.getInteger("id").intValue();
    }

    /**
     * Returns a string representation of this result.
     * @return a string representation of this result
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Error Code: ");
        sb.append(getErrorCode());
        sb.append("\nError Message: ");
        sb.append(getErrorMessage());

        if(items.size() > 0) {
            sb.append("\n");

            for(TS3Map item : items) {
                sb.append(item.toString());
                sb.append("\n");
            }
        }

        return sb.toString();
    }

}

