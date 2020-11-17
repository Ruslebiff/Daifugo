package server;

import server.exceptions.*;

import java.io.Serializable;
import java.util.*;

public class UserSession implements Serializable {
    private static Map<UUID, UserSession> sessions = new HashMap<>();
    private static Set<String> nickList = new HashSet<>();
    private static long userCount = 0L;

    private final UUID token;
    private String nick;
    private Game currentGame;

    public UserSession() {
        synchronized (UserSession.class) {
            token = UUID.randomUUID();
            currentGame = null;
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

    public static UserSession retrieveSessionFromID(
            UUID id
    ) throws UserSessionError {

        synchronized (UserSession.class) {
            if (!sessions.containsKey(id))
                throw new UserSessionError(
                        "Trying to retrieve non-existing session"
                );

            return sessions.get(id);
        }
    }

    public static UserSession retrieveSessionFromToken(String token) throws UserSessionError {
        return UserSession.retrieveSessionFromID(UUID.fromString(token));
    }

    public String getToken() {
        synchronized (this) {
            return token.toString();
        }
    }

    public UUID getID() {
        return token;
    }

    public String getNick() {
        synchronized (this) {
            return nick;
        }
    }

    public void setNick(String nick) throws UserSessionError {
        synchronized (UserSession.class) {
            if (nickList.contains(nick)) {
                throw new UserSessionError("Nick already used");
            }

            nickList.remove(this.nick);
            nickList.add(nick);
        }
        synchronized (this) {
            this.nick = nick;
        }
    }

    public void endSession() {
        synchronized (UserSession.class) {
            sessions.remove(token);
            nickList.remove(nick);
        }
    }


    public void joinGame(UUID gameID) throws GameException {
        Game game = Game.getGameByID(gameID);
        if (game == null)
            throw  new GameException("No game by ID: " + gameID.toString());
        currentGame = game;
    }

    public Game getGame() {
        return currentGame;
    }

    public void leaveCurrentGame() {
            currentGame.leaveGame(getID());
            currentGame = null;
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


