package server;

import java.util.UUID;

public class UserSession {
    private final UUID token;

    public UserSession() {
        token = UUID.randomUUID();
    }

    public UserSession(String token) {
        this.token = UUID.fromString(token);
    }

    public String getToken() {
        return token.toString();
    }

    public void joinGame(UUID gameID) {
        // TODO
    }

    public UUID getGameID() {
        //TODO
        return UUID.randomUUID();
    }
}


