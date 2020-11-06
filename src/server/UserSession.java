package server;

import server.exceptions.UserSessionError;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserSession {
    private static final Map<UUID, UserSession> sessions = new HashMap<>();

    private final UUID token;

    public UserSession() {
        token = UUID.randomUUID();
        sessions.put(token, this);
    }

    public UserSession(String token) throws UserSessionError {
        if (sessions.containsKey(UUID.fromString(token)))
            throw new UserSessionError("Trying to duplicate session");

        this.token = UUID.fromString(token);
        sessions.put(this.token, this);
    }

    public static UserSession retrieveSessionFromToken(String token) throws UserSessionError {
        UUID id = UUID.fromString(token);
        if (!sessions.containsKey(id))
            throw new UserSessionError("Trying to retrieve non-existing session");
        return sessions.get(id);
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


