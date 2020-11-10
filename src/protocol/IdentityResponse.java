package protocol;

import server.UserSession;

public class IdentityResponse extends Message {
    private String token;
    private String nick;

    public IdentityResponse(String token, String nick) {
        super(MessageType.IDENTITY_RESPONSE);
        this.token = token;
        this.nick = nick;
    }

    public String getToken() {
        return token;
    }

    public String getNick() {
        return nick;
    }
}
