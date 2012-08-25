# TS3 Server Query

Provides a low-level library for communicating with a Teamspeak 3 server through
the Teamspeak 3 Server Query.

## Examples

Implementing is fairly straightforward, simply instantiate a
`TS3ServerQueryClient` object, connect, and execute commands freely.

package net.visualcoding.ts3serverquery.example;

import net.visualcoding.ts3serverquery.TS3ServerQueryClient;
import net.visualcoding.ts3serverquery.TS3Result;
import net.visualcoding.ts3serverquery.TS3Map;

```java
public class ClientListExample {

    public static void main(String[] args) throws Exception {
        if(args.length < 4) {
            System.err.println("Usage: java ClientListExample <host> <port> " +
                    "<username> <password");
            System.exit(1);
        }

        // Instantiate a client object
        TS3ServerQueryClient client = new TS3ServerQueryClient(args[0],
                Integer.parseInt(args[1]));

        // Connect to the server
        client.connect();

        // Build a map for the arguments to our login command
        TS3Map arguments = new TS3Map();
        arguments.add("client_login_name", args[2]);
        arguments.add("client_login_password", args[3]);

        // Execute the login command
        TS3Result result = client.execute("login", arguments);

        // Check if we were able to successfully log in
        if(result.hasError()) {
            System.err.println("Login failed");
            System.exit(2);
        } else {
            System.out.println("Login successful");
        }

        // Switch over to virtual server 1
        result = client.execute("use sid=1");

        // Issue the clientinfo command to get all the clients in the server
        result = client.execute("clientlist");

        // Output the results
        System.out.println(result);

        // Disconnect from the client and terminate program
        client.disconnect();
    }

}
```

# Known Bugs

... I'll add this bit in later.
