package server;

import common.CardData;
import common.GameListing;
import server.exceptions.GameException;
import server.exceptions.GameInProgress;
import server.exceptions.PlayerAlreadyInGame;
import server.exceptions.UserSessionError;

import java.util.*;

public class Game {
    private static Map<UUID, Game> games = new LinkedHashMap<>();

    public static List<GameListing> getGameList() throws UserSessionError {
        List<GameListing> list = new ArrayList<>();

        for (UUID id : games.keySet()) {
            Game game = games.get(id);
            list.add(
                    new GameListing(
                            id.toString(),
                            game.title,
                            game.getOwnerNick(),
                            game.players.size(),
                            !game.password.equals(""),
                            game.started
                    )
            );
        }

        return list;
    }

    public static Game getGameByID(UUID id) {
        return games.get(id);
    }

    /**
     * Internal reset of state for testing
     */
    public static void _reset() {
        games = new LinkedHashMap<>();
    }

    private final UUID ID;
    private final UUID owner;
    private final String title;
    private final String password;
    private final Map<UUID, UserSession> players;
    private int currentPlayer;
    private boolean started;
    private final Map<UUID, List<CardData>> hands;

    public Game(
            UUID owner,
            String title,
            String password
    ) throws UserSessionError, GameException {
        ID = UUID.randomUUID();
        this.owner = owner;
        this.title = title;
        this.password = password;
        players = new HashMap<>();
        UserSession ownerSession = UserSession.retrieveSessionFromID(owner);
        joinGame(ownerSession);
        hands = new HashMap<>();
        started = false;

        games.put(ID, this);
    }

    public UUID getID() {
        return ID;
    }

    public String getTitle() {
        return title;
    }

    public boolean hasStarted() {
        return started;
    }

    public String getOwnerNick() throws UserSessionError {
        return UserSession.retrieveSessionFromID(owner).getNick();
    }

    public void joinGame(UserSession user) throws GameException {
        if (started)
            throw new GameInProgress();

        if (players.containsKey(user.getID()))
            throw new PlayerAlreadyInGame();

        players.put(user.getID(), user);
        user.joinGame(ID);
    }


    // TODO: game start
    // TODO: game rounds
    // TODO: game turns
    // TODO: round end
        // TODO: will players continue?


    // TODO: game end
    public void endGame() {
        games.remove(ID);
    }
}
