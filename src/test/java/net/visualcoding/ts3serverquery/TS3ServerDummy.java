package net.visualcoding.ts3serverquery;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class TS3ServerDummy extends Thread {
    protected int port;
    protected ServerSocket serverSocket;
    protected TS3Writer writer;
    protected TS3Reader reader;

    protected Map<Integer, TS3Map> clients;
    protected Set<String> notifications;

    protected Random random;
    protected SecureRandom secureRandom;

    protected Semaphore semEventReceived;
    protected Semaphore semEvents;

    public TS3ServerDummy(int port) {
        this.port = port;

        random = new Random();
        secureRandom = new SecureRandom();

        // Instantiate client map and create some fake clients
        clients = Collections.synchronizedMap(new HashMap<Integer, TS3Map>());
        for(int i = 1; i <= 25; i++) {
            TS3Map client = createRandomClient(i);

            clients.put(i, client);
        }

        // Instantiate the notifications set, which will contain all the
        // notifications that were registered
        notifications = new HashSet<String>();

        // Initialize binary semaphores
        semEventReceived = new Semaphore(0);
        semEvents = new Semaphore(0);
    }

    protected TS3Map createRandomClient(int id) {
        TS3Map client = new TS3Map();

        client.add("clid", id);
        client.add("client_nickname", "client" + Integer.toString(id));
        client.add("client_unique_identifier", (new BigInteger(130,
                secureRandom)).toString(32));
        client.add("cid", random.nextInt(10) + 1);

        return client;
    }

    public void run() {
        try {
            runServer();
        } catch(Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    private void runServer() throws Exception {
        // Start listening on our port
        serverSocket = new ServerSocket(port);

        // Get the first client, no need for multiple client support
        Socket socket = serverSocket.accept();

        // Instantiate writer/reader objects
        writer = new TS3Writer(new OutputStreamWriter(socket.getOutputStream()));
        reader = new TS3Reader(new InputStreamReader(socket.getInputStream()));

        // Send in the first two lines, the welcome messages
        write("TS3");
        write("Hello world!");

        // Spawn thread that makes changes to the client map to test the polling
        // thread functionality
        Thread t = new Thread(new ClientMapChanger(this));
        t.start();

        // Listen for input
        String input;
        while((input = reader.readLine()) != null) {
            // Split by whitespace
            String[] split = input.split("\\s+", 2);

            System.out.println("Client: " + input);

            // We're going to do something nasty here, but just for convenience
            Class<?> c = getClass();

            try {
                // Try to obtain a method in the form of command_COMMAND.
                // For example, clientlist will call command_clientlist
                Method method = c.getDeclaredMethod("command_" + split[0], String.class);

                // Invoke method
                method.invoke(this, split[1]);
            } catch(NoSuchMethodException e) {
                // Command not found error
                writeError(256, "command not found");
            }
        }

    }

    protected void command_login(String cmd) throws Exception {
        writeError();
    }

    protected void command_clientlist(String cmd) throws Exception {
        StringBuilder sb = new StringBuilder();

        Iterator<TS3Map> it = clients.values().iterator();
        while(it.hasNext()) {
            TS3Map client = it.next();
            sb.append(client.toString());
            if(it.hasNext()) sb.append("|");
        }

        write(sb.toString());
        writeError();
    }

    protected void command_servernotifyregister(String cmd) throws Exception {
        TS3Map map = new TS3Map(cmd);

        // Get the event name
        String event = map.get("event");

        // Don't do anything if the event is channel and a channel id is not
        // passed
        if(event.equalsIgnoreCase("channel")) {
            if(map.contains("id")) return;
        }

        // Add event to our notifications set
        notifications.add(event);

        writeError();
    }

    protected void write(String line) throws Exception {
        getWriter().writeLine(line);
        // System.out.println("Server: " + line);
    }

    protected void writeError() throws Exception {
        writeError(0, "ok");
    }

    protected void writeError(int id, String msg) throws Exception {
        write(String.format("error id=%d msg=%s", id, TS3Map.escape(msg)));
    }

    protected TS3Writer getWriter() {
        return writer;
    }

    protected TS3Reader getReader() {
        return reader;
    }

    public boolean hasClient(int id) {
        return clients.containsKey(new Integer(id));
    }

    public boolean isClientInChannel(int id, int channel) {
        TS3Map client = clients.get(new Integer(id));

        if(client == null) return false;
        return client.getInteger("cid").intValue() == channel;
    }

    public boolean isEventRegistered(String event) {
        return notifications.contains(event);
    }

    private class ClientMapChanger implements Runnable {
        TS3ServerDummy server;

        public ClientMapChanger(TS3ServerDummy server) {
            this.server = server;
        }

        public void run() {
            try {
                runChanger();
            } catch(InterruptedException e) {
                // blah
            }
        }

        protected void runChanger() throws InterruptedException {
            // Wait until the polling thread for the client starts up
            Thread.sleep(1000);

            // Remove a client from the map
            server.clients.remove(1);
            waitForResponse();

            // Add in a new client
            TS3Map client = server.createRandomClient(30);
            server.clients.put(client.getInteger("clid"), client);
            waitForResponse();

            // Change the channel of a client
            client = server.clients.get(2);
            int channel = client.getInteger("cid").intValue();
            client.remove("cid");
            client.add("cid", channel + 1);
            waitForResponse();

            // Release semaphore for all events
            semEvents.release();
        }

        protected void waitForResponse() throws InterruptedException {
            // Wait until the event was received
            assert semEventReceived.tryAcquire(5000, TimeUnit.MILLISECONDS);
        }
    }
}
