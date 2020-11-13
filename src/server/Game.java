package server;

import common.CardData;
import common.GameListing;
import common.PlayerData;
import server.exceptions.*;

import java.util.*;

public class Game {
    private static Map<UUID, Game> games = new LinkedHashMap<>();

    public static List<GameListing> getGameList() throws UserSessionError, GameException {
        if (games.size() == 0)
            throw new GameException("currently no games");

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
     * Internal reset of static state for testing
     */
    public static void _reset() {
        games = new LinkedHashMap<>();
    }

    private final UUID ID;
    private final UUID owner;
    private final String title;
    private final char[] password;
    private final Map<UUID, PlayerObject> players;
    private int currentPlayer;
    private boolean started;
    private final Map<UUID, List<CardData>> hands;

    public Game(
            UUID owner,
            String title,
            char[] password
    ) throws UserSessionError, GameException {
        ID = UUID.randomUUID();
        this.owner = owner;
        this.title = title;
        this.password = password;
        players = new HashMap<>();
        hands = new HashMap<>();
        started = false;

        games.put(ID, this);
        UserSession ownerSession = UserSession.retrieveSessionFromID(owner);
        try {
            joinGame(ownerSession, password);
        } catch (WrongPassword wrongPassword) {
            wrongPassword.printStackTrace();
        }
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

    public void joinGame(
            UserSession user,
            char[] password
    ) throws GameException, WrongPassword {
        if (started)
            throw new GameInProgress();

        if (players.containsKey(user.getID()))
            throw new PlayerAlreadyInGame();

        if (!Arrays.equals(password, this.password)) throw new WrongPassword();

        PlayerData data = new PlayerData(
                user.getNick(),
                0,
                false,
                0,
                0
        );

        players.put(user.getID(), new PlayerObject(user, data));
        user.joinGame(ID);
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public List<CardData> getPlayerHand(UUID playerID) {
        return hands.get(playerID);
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
