package net.visualcoding.ts3serverquery.event;

import net.visualcoding.ts3serverquery.TS3EventListener;

public class TS3MessageEvent extends TS3Event {
    private Mode mode;
    private String message;

    public TS3MessageEvent(String clientName, int clientId, String clientUid) {
        super(clientName, clientId, clientUid);
    }

    public Mode getMode() {
        return mode;
    }

    public String getMessage() {
        return message;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setMode(int value) {
        setMode(Mode.getMode(value));
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void execute(TS3EventListener listener) {
        listener.onMessage(this);
    }

    public static enum Mode {
        Server(1),
        Channel(2),
        Private(3);

        private final int value;

        Mode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Mode getMode(int value) {
            for(Mode m : Mode.values()) {
                if(m.getValue() == value) return m;
            }
            return null;
        }
    }
}
