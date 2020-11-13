package server;

import common.CardData;
import common.GameListing;
import common.PlayerData;
import server.exceptions.*;

import java.util.*;

public class Game {

    /**
     * Static stuff
     ***********************/


    private static Map<UUID, Game> games = new LinkedHashMap<>();

    public static List<GameListing> getGameList() throws UserSessionError, GameException {
        synchronized (Game.class) {
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
                                game.password != null,
                                game.started
                        )
                );
            }

            return list;
        }
    }

    public static Game getGameByID(UUID id) {
        synchronized (Game.class) {
            return games.get(id);
        }
    }

    // internal reset of static variables for testing
    public static void _reset() {
        synchronized (Game.class) {
            games = new LinkedHashMap<>();
        }
    }


    /**
     * Data members
     ***********************/
    private final UUID ID;
    private final UUID owner;
    private final String title;
    private final char[] password;
    private final Map<UUID, PlayerObject> players;
    private int currentPlayer;
    private boolean started;
    private final Map<UUID, List<CardData>> hands;
    private boolean cancelled;


    /**
     * Public interface
     ***************************/


    /**
     * Constructor
     * @param owner UUID of the game's creator
     * @param title String title of the game
     * @param password char[] password, or null if game is public
     * @throws UserSessionError that propagates from calls to owners UserSession
     * @throws GameException in the very unlikely event that owner is already in the game
     */
    public Game(
            UUID owner,
            String title,
            char[] password
    ) throws UserSessionError, GameException {
        ID = UUID.randomUUID();
        this.owner = owner;
        this.title = title;
        this.password = password;
        cancelled = false;
        players = new HashMap<>();
        hands = new HashMap<>();
        started = false;

        synchronized (Game.class) {
            games.put(ID, this);
        }

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

    public Map<UUID, PlayerObject> getPlayers() {
        synchronized (this) {
            return players;
        }
    }

    public String getTitle() {
        return title;
    }

    public boolean hasStarted() {
        synchronized (this) {
            return started;
        }
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

        synchronized (this) {
            players.put(user.getID(), new PlayerObject(user, data));
            user.joinGame(ID);
        }
    }

    public int getCurrentPlayer() {
        synchronized (this) {
            return currentPlayer;
        }
    }

    public List<CardData> getPlayerHand(UUID playerID) {
        synchronized (this) {
            return hands.get(playerID);
        }
    }

    public void cancelGame() {
        synchronized (this) {
            cancelled = true;
            leaveGame(owner);
            propagateChange();
        }
    }

    public boolean isCancelled() {
        synchronized (this) {
            return cancelled;
        }
    }

    public void leaveGame(UUID player) {
        synchronized (this) {
            players.remove(player);
            if (players.size() == 0) {
               removeFromList();
            }
        }
    }

    public void registerGameStateChange() {
        synchronized (this) {
            propagateChange();
        }
    }


    // TODO: game start
    // TODO: game rounds
    // TODO: game turns
    // TODO: round end
        // TODO: will players continue?


    /**
     * Private helpers
     ************************/


    // TODO: removing game from list when done with it
    private void removeFromList() {
        synchronized (Game.class) {
            games.remove(ID);
        }
    }

    private void propagateChange() {
        for (PlayerObject player : players.values()) {
            player.newStateAvailable();
        }
    }

    private void nextPlayer() {
        //TODO: make loop-around code
        currentPlayer++;
    }



    /**
     * Game mechanics
     */

    private void dealCards() {
        //TODO
    }

    private void shufflePlayerOrder() {
        //TODO
    }

    private void findStartingPlayer() {
        //TODO: depends on roles, or location of three of diamonds if first round
    }
}
