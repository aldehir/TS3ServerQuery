package net.visualcoding.ts3serverquery;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;
import java.util.TreeSet;
import java.lang.StringBuilder;

/**
 * TS3Command.
 *
 * @author Aldehir Rojas
 * @version 1.0
 * @deprecated Unnecessary, use {@link TS3Map} in conjunction with
 *             {@link TS3ServerQueryClient#execute(String, TS3Map)}.
 */
@Deprecated
public class TS3Command {

    private String command;
    private ArrayList<String> positionalArguments;
    private Map<String, Set<String>> arguments;

    public TS3Command(String command) {
        setCommand(command);

        // Create data structures with a reasonable initial capacity
        positionalArguments = new ArrayList<String>(3);
        arguments = new HashMap<String, Set<String>>(7);
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public TS3Command add(String argument) {
        return add(argument, false);
    }

    public TS3Command add(String argument, boolean isSwitch) {
        if(isSwitch) {
            add(argument, null);
        } else {
            positionalArguments.add(argument);
        }

        return this;
    }

    public TS3Command add(String argument, String value) {
        if(value == null) {
            arguments.put(argument, null);
            return this;
        }

        if(arguments.containsKey(argument)) {
            // Get the set of the option
            Set<String> set = arguments.get(argument);

            // Add in our value
            set.add(value);
        } else {
            // Create a tree set and add our value to it
            TreeSet<String> set = new TreeSet<String>();
            set.add(value);

            // Set the option to our set
            arguments.put(argument, set);
        }

        return this;
    }

    public TS3Command add(String argument, int value) {
        return add(argument, Integer.toString(value));
    }

    public TS3Command replace(String argument, String value) {
        remove(argument);
        return add(argument, value);
    }

    public TS3Command replace(String argument, int value) {
        return replace(argument, Integer.toString(value));
    }

    public void remove(String argument) {
        arguments.remove(argument);
    }

    public void clear() {
        positionalArguments.clear();
        arguments.clear();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Add in the command name
        sb.append(getCommand());

        // Add in all of the positional arguments
        for(String arg : positionalArguments) {
            sb.append(' ');
            sb.append(TS3Util.escape(arg));
        }

        // Add in all of the other arguments
        for(Map.Entry<String, Set<String>> entry : arguments.entrySet()) {
            sb.append(' ');

            if(entry.getValue() == null) {
                // Add in as a switch
                sb.append('-');
                sb.append(entry.getKey());
                continue;
            }

            // Get the iterator for our set
            Iterator<String> iter = entry.getValue().iterator();
            while(iter.hasNext()) {
                // Add in the argument
                sb.append(entry.getKey());
                sb.append('=');
                sb.append(TS3Util.escape(iter.next()));

                // Add in a pipe if there are more values in our set
                if(iter.hasNext()) sb.append('|');
            }
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        TS3Command cmd = new TS3Command("login");
        cmd.add("serveradmin");
        cmd.add("password");

        System.out.println(cmd.toString());

        cmd = new TS3Command("cmd");
        cmd.add("clid", 6);
        cmd.add("clid", 3);
        cmd.add("clid", 4);
        cmd.add("msg", "This is a message");
        cmd.add("channelId", "12");
        cmd.add("uid", true);

        System.out.println(cmd.toString());

        cmd.remove("channelId");

        System.out.println(cmd.toString());
    }
}
