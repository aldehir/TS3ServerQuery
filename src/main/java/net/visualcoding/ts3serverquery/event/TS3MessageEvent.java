package net.visualcoding.ts3serverquery.event;

import net.visualcoding.ts3serverquery.TS3EventListener;

/**
 * This class represents a message sent to the server query client from the
 * Teamspeak 3 server. This message may be one of three modes: private, server,
 * or channel. Modes are representing using the {@link TS3MessageEvent#Mode}
 * enum.
 *
 * @author Aldehir Rojas
 * @version 1.0
 */
public class TS3MessageEvent extends TS3Event {

    /** Target mode of the message for this event. */
    private Mode mode;

    /** Message sent to the query client for this event. */
    private String message;

    /**
     * Constructs a message event object with all string fields set to null
     * and int fields to 0.
     */
    public TS3MessageEvent() {
        this(null, 0, null, null, null);
    }

    /**
     * Constructs a message event object with the specified client details.
     *
     * @param clientName name of the client that triggered this event
     * @param clientId   id of the client that triggered this event
     * @param clientUid  unique id of the client that triggered this event
     */
    public TS3MessageEvent(String clientName, int clientId, String clientUid) {
        this(clientName, clientId, clientUid, null, null);
    }

    /**
     * Constructs a message event object with the specified client and message
     * details.
     *
     * @param clientName name of the client that triggered this event
     * @param clientId   id of the client that triggered this event
     * @param clientUid  unique id of the client that triggered this event
     * @param message    message sent by the client
     * @param mode       mode of the message sent by the client
     */
    public TS3MessageEvent(String clientName, int clientId, String clientUid,
            String message, int mode) {
        this(clientName, clientId, clientUid, message, Mode.getMode(mode));
    }

    /**
     * Constructs a message event object with the specified client and message
     * details.
     *
     * @param clientName name of the client that triggered this event
     * @param clientId   id of the client that triggered this event
     * @param clientUid  unique id of the client that triggered this event
     * @param message    message sent by the client
     * @param mode       mode of the message sent by the client
     */
    public TS3MessageEvent(String clientName, int clientId, String clientUid,
            String message, Mode mode) {
        super(clientName, clientId, clientUid);
        setMessage(message);
        setMode(mode);
    }

    /**
     * Returns the mode of the message from this message event.
     * @return the mode of the message from this message event
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Returns the message from this message event.
     * @return the message from this message event
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the mode of the message for this message event.
     * @param mode mode of the message
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Sets the mode of the message for this message event. This is the
     * equivalent of calling {@code setMode(Mode.getMode(value))}.
     * @param value int value of the mode of this message
     */
    public void setMode(int value) {
        setMode(Mode.getMode(value));
    }

    /**
     * Sets the message of this message event.
     * @param message message of this message event
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Calls {@link TS3EventListener#onMessage()} and passes this message event.
     */
    public void execute(TS3EventListener listener) {
        listener.onMessage(this);
    }

    /**
     * Target mode of messages.
     */
    public static enum Mode {
        /** Text message received in the global server channel. */
        Server(3),
        /** Text message received in the channel the query client is in. */
        Channel(2),
        /** Text message received via private message. */
        Private(1);

        /** Integer value of this mode. */
        private final int value;

        /**
         * Constructs this Mode object with the specified integer value.
         * @param value value of this mode
         */
        Mode(int value) {
            this.value = value;
        }

        /**
         * Returns the integer value of this mode.
         * @return the integer value of this mode
         */
        public int getValue() {
            return value;
        }

        /**
         * Returns the Mode constant represented by the specified integer value.
         * If no such mode is mapped to the specified value, then {@code null}
         * is returned.
         *
         * @param value value of the Mode constant that is to be returned
         * @return the Mode constant representing the specified value if it
         *         exists, {@code null} otherwise.
         */
        public static Mode getMode(int value) {
            for(Mode m : Mode.values()) {
                if(m.getValue() == value) return m;
            }
            return null;
        }
    }
}
