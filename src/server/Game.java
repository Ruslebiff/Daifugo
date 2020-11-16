package server;

import common.*;
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
    private List<CardData> cardsOnTable;
    private int noOfCardsFaceDown;  // number of cards removed with previous tricks
    private int noOfCardsInTrick;   // number of cards being played
    private boolean cancelled;
    private List<UUID> turnSequence;
    private int goneOut;        // increments for each player who goes out, resets each round


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
        cardsOnTable = new ArrayList<>();
        noOfCardsFaceDown = 0;
        noOfCardsInTrick = 0;
        turnSequence = new ArrayList<>();
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

    public int getNoOfCardsFaceDown() {
        synchronized (this) {
            return noOfCardsFaceDown;
        }
    }

    public int getNoOfCardsInTrick() {
        return noOfCardsInTrick;
    }

    /**
     * Internal non-synchronized method for getting top 4 cards
     * @return
     */
    private List<CardData> _getTopCards() {
        List<CardData> tail = cardsOnTable.subList(
                Math.max(cardsOnTable.size() - 4, 0), cardsOnTable.size()
        );
        return tail;
    }

    public List<CardData> getTopCards() {
        List<CardData> tmp;
        synchronized (this) {
            tmp = _getTopCards();
        }
        tmp.remove(0);  // only return top 3 to clients
        return tmp;
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
                Role.NEUTRAL,
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

    public List<UUID> getTurnSequence() {
        return turnSequence;
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
            turnSequence.remove(player);
            if (players.size() == 0) {
               removeFromList();
            }
        }
        // TODO: call resetRound() if the game is started.
    }

    public void start() {
        synchronized (this) {
            started = true;
            shufflePlayerOrder();
            findStartingPlayer(dealCards());
            propagateChange();
        }
    }

    private void assignRoles() {
        if (players.size() == 3) {
            for (PlayerObject po : players.values()) {
                po.getGameData().assignRoleFewPlayers();
            }
        } else {
            for (PlayerObject po : players.values()) {
                po.getGameData().assignRoleManyPlayers(players.size());
            }
        }
    }

    public void playCards(UUID player, List<CardData> cards) {
        // TODO: if cards not allowed to be played, throw exception
        try {
            synchronized (this) {
                cardsOnTable.addAll(cards);
                hands.get(player).removeAll(cards);

                // check if there is a new trick from playing
                List<CardData> topCards = _getTopCards();
                if (cards.get(0).getValue() == 16) {
                    newTrick();
                    return;
                } else if (topCards.size() == 4
                        && topCards.get(1) == topCards.get(0)
                        && topCards.get(2) == topCards.get(0)
                        && topCards.get(3) == topCards.get(0)
                ) {
                    // all 4 topcards the same, start new trick
                    newTrick();
                    return;
                }
                    noOfCardsInTrick = cards.size(); // TODO: should add some verification here
                    nextPlayer();
                    propagateChange();
            }
        } catch (RoundOver roundOver) {
            newRound();
        }
    }
    public void pass(UUID player) throws RoundOver {
        synchronized (this) {
            players.get(player).getGameData().setPassed(true);
            nextPlayer();
            propagateChange();
        }
     }

    public void newRound() {
        synchronized (this) {
            assignRoles();
            findStartingPlayer(dealCards());
            propagateChange();
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

    private List<CardData> prepareDeck() {
        List<CardData> deck = new ArrayList<>();

        char[] suits = {'H', 'S', 'C', 'D'}; // H(earts), S(pades), C(lubs), D(iamond)
        for (int suit = 0; suit < 4; suit++)        // For each suit, create 13 cards
            for (int number = 2; number < 15; number++)
                deck.add(new CardData(number, suits[suit]));    // Add the card to the cardList
        Collections.shuffle(deck);          // Shuffle the cards

        return deck;
    }


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


    private void nextPlayer() throws RoundOver {

        // only one player left -- round is over
        if (goneOut == players.size()-1) {

            // find the last remaining player, and set outcount
            for (UUID id : hands.keySet()) {
                if (hands.get(id).isEmpty()) {
                    players.get(id).getGameData().setOutCount(players.size());
                    break;
                }
            }

            throw new RoundOver();
        }

        // find the next player in turn order who hasn't passed or gone out
        PlayerData pd;
        do {
            pd = players.get(turnSequence.get(++currentPlayer)).getGameData();
        } while (pd.hasPassed() || pd.isOutOfRound());

    }

    private void newTrick() {
        for (PlayerObject po : players.values()) {
            po.getGameData().setPassed(false);
        }
    }

    private void resetRound() {
        // TODO: this function is called when a player has left, so resetting round
        // sets all roles back to neutral, and starts a new round with remaining
        // players, if enough.
    }



    /**
     * Game mechanics
     **************************************/


    /**
     * Populates the player hands with new cards.
     *
     * Turn sequence must be determined before calling this function.
     *
     * @return UUID id of the hand with the 3 of diamonds
     */
    private UUID dealCards() {
        int player = 0;
        UUID playerWithThreeOfDiamonds = null;
        UUID tmp;


        // empties all hands
        for (UUID hand : hands.keySet()) {
            hands.remove(hand);
            hands.put(hand, new ArrayList<>());
        }

        // deals new cards
        for (CardData card : prepareDeck()) {

            if (player == turnSequence.size())
                player = 0;

            tmp = turnSequence.get(player++);
            hands.get(tmp).add(card);

            if (playerWithThreeOfDiamonds == null
                    && card.getNumber() == 3
                    && card.getSuit() == 'D'
            ) {
                playerWithThreeOfDiamonds = tmp;
            }
        }

        return playerWithThreeOfDiamonds;
    }

    private void shufflePlayerOrder() {
        turnSequence = new ArrayList<>();
        turnSequence.addAll(players.keySet());
        Collections.shuffle(turnSequence);
    }

    /**
     * Setting currentPlayer for the start of a round.
     *
     * MUST be called inside a 'synchronized' block!
     *
     * @param threeOfDiamonds UUID ID of player having the three of diamonds
     */
    private void findStartingPlayer(UUID threeOfDiamonds) {

        currentPlayer = -1;
        for (UUID id : turnSequence) {
            if (players.get(id).getGameData().getRole() == Role.BUM) {
                currentPlayer = turnSequence.indexOf(id);
                break;
            }
        }

        if (currentPlayer < 0)
            currentPlayer = turnSequence.indexOf(threeOfDiamonds);
    }
}
