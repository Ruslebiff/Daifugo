package server;

import server.exceptions.UserSessionError;
import java.util.*;

public class UserSession {
    private static Map<UUID, UserSession> sessions = new HashMap<>();
    private static Set<String> nickList = new HashSet<>();
    private static long userCount = 0L;

    private final UUID token;
    private String nick;

    public UserSession() {
        token = UUID.randomUUID();

        synchronized (UserSession.class) {
            sessions.put(token, this);
            userCount++;
            nick = "User" + userCount;
            nickList.add(nick);
        }
    }

    public UserSession(String token) throws UserSessionError {
        this.token = UUID.fromString(token);

        synchronized (UserSession.class) {
            if (sessions.containsKey(this.token))
                throw new UserSessionError("Trying to duplicate session");
            sessions.put(this.token, this);
        }
    }

    public static UserSession retrieveSessionFromToken(
            String token
    ) throws UserSessionError {
        UUID id = UUID.fromString(token);

        synchronized (UserSession.class) {
            if (!sessions.containsKey(id))
                throw new UserSessionError(
                        "Trying to retrieve non-existing session"
                );

            return sessions.get(id);
        }
    }

    public String getToken() {
        return token.toString();
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) throws UserSessionError {
        synchronized (UserSession.class) {
            if (nickList.contains(nick)) {
                throw new UserSessionError("Nick already used");
            }

            nickList.remove(this.nick);
            nickList.add(nick);
        }
        this.nick = nick;
    }


    public void joinGame(UUID gameID) {
        // TODO
    }

    public UUID getGameID() {
        //TODO
        return UUID.randomUUID();
    }

    public void leaveCurrentGame() {
        // TODO
    }

    /**
     * Needed for internal diagnostics and testing - resets all static variables
     * NOTE: Should not be used externally
     */
    public static void _reset() {
        synchronized (UserSession.class) {
            nickList = new HashSet<>();
            sessions = new HashMap<>();
            userCount = 0L;
        }
    }
}


