package protocol;

public class ReconnectMessage extends Message {
    private final String token;
    public ReconnectMessage(String token) {
        super(MessageType.RECONNECT);

        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
