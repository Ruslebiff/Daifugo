package client.networking;

/**
 * Session class which implements methods for abstract messaging through ClientConnection
 */
public class Session {
    private String token;
    private String nick;
    private ClientConnection connection;
    private int latency;

    public Session(String url) {
        // TODO: create a new connection, supplying no nick or token, sets these
        // TODO: create a separate heartbeat thread, which periodically sends a heartbeat and updates latency
    }

    public String getToken() {
        return token;
    }

    public String getNick() {
        return nick;
    }

    public int getLatency() {
        return latency;
    }

    public boolean requestNewNick(String nick) {
        // TODO: request the new nick from server, if in use, return false, else true
        return false;
    }
}
