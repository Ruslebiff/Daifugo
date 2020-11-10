package protocol;

import server.UserSession;

public class IdentityResponse extends Message {
    private UserSession session;

    public IdentityResponse(UserSession session) {
        super(MessageType.IDENTITY_RESPONSE);
        this.session = session;
    }

    public String getToken() {
        return session.getToken();
    }

    public String getNick() {
        return session.getNick();
    }
}
