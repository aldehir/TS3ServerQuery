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

public class TS3ServerDummy extends Thread {
    private int port;
    private ServerSocket serverSocket;
    private TS3Writer writer;
    private TS3Reader reader;
    
    private HashMap<Integer, Client> clients;
    private HashSet<String> notifications;
    
    public TS3ServerDummy(int port) {
        this.port = port;
        
        Random random = new Random();
        SecureRandom secureRandom = new SecureRandom();
        
        // Instantiate client map and create some fake clients
        clients = new HashMap<Integer, Client>();
        for(int i = 1; i <= 25; i++) {
            Client client = new Client();
            client.name = "client" + Integer.toString(i);
            client.id = i;
            client.uid = (new BigInteger(130, secureRandom)).toString(32);
            client.channel = random.nextInt(10) + 1;
            
            clients.put(client.id, client);
            System.out.println("Added client: " + client.toString());
        }
        
        // Instantiate the notifications set, which will contain all the
        // notifications that were registered
        notifications = new HashSet<String>();
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
        
        Iterator<Client> it = clients.values().iterator();
        while(it.hasNext()) {
            Client client = it.next();
            sb.append(client.toString());
            if(it.hasNext()) sb.append("|");
        }
        
        write(sb.toString());
        writeError();
    }
    
    protected void command_servernotifyregister(String cmd) throws Exception {
        // Parse the map
        Map<String, String> details = TS3Util.parseDetails(cmd);

        // Get the event name
        String event = details.get("event");

        // Don't do anything if the event is channel and a channel id is not
        // passed
        if(event.equalsIgnoreCase("channel")) {
            if(details.containsKey("id")) return;
        }

        // Add event to our notifications set
        notifications.add(event);

        writeError();
    }
    
    protected void write(String line) throws Exception {
        getWriter().writeLine(line);
        System.out.println("Server: " + line);
    }
    
    protected void writeError() throws Exception {
        writeError(0, "ok");
    }
    
    protected void writeError(int id, String msg) throws Exception {
        write(String.format("error id=%d msg=%s", id, TS3Util.escape(msg)));
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
        Client client = clients.get(new Integer(id));
        
        if(client == null) return false;
        return client.channel == channel;
    }

    public boolean isEventRegistered(String event) {
        return notifications.contains(event);
    }
    
    /**
     * Basic client structure
     */
    class Client {
        String name;
        int id;
        String uid;
        int channel;
        
        public String toString() {
            return String.format("clid=%d cid=%d client_nickname=%s client_unique_identifier=%s",
                    id, channel, TS3Util.escape(name), uid);
        }
    }
}
