package server;

import client.Card;
import common.*;
import server.exceptions.*;

import java.util.*;
import java.util.logging.Logger;

import static server.Server.SERVER_LOGGER;


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

            try {
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
            } catch (Exception e) {
                SERVER_LOGGER.info("error inside getGameList: " + e.getMessage());
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
    private Map<UUID, List<CardData>> hands;
    private List<CardData> cardsOnTable;
    private int noOfCardsFaceDown;  // number of cards removed with previous tricks
    private int noOfCardsInTrick;   // number of cards being played
    private boolean cancelled;
    private List<UUID> turnSequence;
    private int goneOut;        // increments for each player who goes out, resets each round
    private int passCount;
    private int roundNo;
    private int playersInTradingPhase;
    private Map<UUID, List<CardData>> receiveFromTrade;
    private Trick trickTriggered;

    //TODO: this todo is just a bookmark
    private final boolean TEST_MODE = true;
    private final int TEST_CARD_NUM = 3;

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
        currentPlayer = -1;
        goneOut = 0;
        started = false;
        passCount = 0;
        roundNo = 1;
        playersInTradingPhase = 0;
        receiveFromTrade = new HashMap<>();
        trickTriggered = Trick.NONE;

        synchronized (Game.class) {
            games.put(ID, this);
        }

        UserSession ownerSession = UserSession.retrieveSessionFromID(owner);
        try {
            joinGame(ownerSession, password);
        } catch (WrongPassword wrongPassword) {
            SERVER_LOGGER.warning("Owner got wrong password when joining own game");
        }
    }

    public boolean isTradingPhase() {
        synchronized (this) {
            return playersInTradingPhase > 0;
        }
    }

    private void decrementTraders() throws GameException {
        if (playersInTradingPhase == 0)
            throw new GameException();
        playersInTradingPhase--;

        // making sure players receive the given cards
        if (playersInTradingPhase == 0) {
            SERVER_LOGGER.info("Trading phase is over");
            for (UUID pID : receiveFromTrade.keySet()) {
                List<CardData> hand = hands.get(pID);
                hand.addAll(receiveFromTrade.get(pID));

            }

            findStartingPlayer(UUID.randomUUID()); // if three of diamonds is chosen, theres a bug
        }
    }

    public void giveCards(UUID player, List<CardData> givenCards) throws GameException {
        synchronized (this) {
            if (!isTradingPhase())
                throw new GameException("Not in trading phase");

            PlayerData playerData = players.get(player).getGameData();
            if (!playerData.hasToTrade())
                throw new GameException("Player no longer needs to trade");


            Role recipientRole;
            switch (playerData.getRole()) {
                case BUM -> recipientRole = Role.PRESIDENT;
                case VICE_BUM -> recipientRole = Role.VICE_PRESIDENT;
                case VICE_PRESIDENT -> recipientRole = Role.VICE_BUM;
                case PRESIDENT -> recipientRole = Role.BUM;
                default -> throw new GameException("Invalid role");
            }

            // removing cards from own hand
            List<CardData> hand = hands.get(player);
            for (CardData card : givenCards) {
                for (CardData c : hand) {
                    if (card.getSuit() == c.getSuit() && card.getNumber() == c.getNumber()) {
                        hand.remove(c);
                        break;
                    }
                }
            }

            // put given cards into receive map
            for (UUID pID : players.keySet()) {
                if (players.get(pID).getGameData().getRole() == recipientRole) {
                    SERVER_LOGGER.fine("Adding given cards to receive map");
                    receiveFromTrade.put(pID, givenCards);
                    break;
                }
            }


            playerData.doneTrading();
            decrementTraders();
            propagateChange();
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
        synchronized (this) {
            return noOfCardsInTrick;
        }
    }

    /**
     * Internal non-synchronized method for getting top 4 cards
     * @return
     */
    private List<CardData> _getTopCards() {
        if (cardsOnTable.size() == 0) {
            return new ArrayList<>();
        }

        int lowIndex = cardsOnTable.size();
        int high = lowIndex;
        while (lowIndex - 1 >= 0 && high - lowIndex < 4)
            lowIndex--;


        return new ArrayList<>(cardsOnTable.subList(
                lowIndex, high
        ));
    }

    public List<CardData> getTopCards() {
        List<CardData> tmp;
        synchronized (this) {
            tmp = _getTopCards();
        }

        SERVER_LOGGER.fine("Size of top cards: " + tmp.size());

        if (tmp.size() == 4)
            tmp.remove(0);
        return tmp;
    }

    public List<CardData> getCardsOnTable() {
        synchronized (this) {
            return cardsOnTable;
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
                Role.NEUTRAL,
                0
        );

        synchronized (this) {
            players.put(user.getID(), new PlayerObject(user, data));
            user.joinGame(ID);
            shufflePlayerOrder();
            propagateChange();
        }
    }

    public int getCurrentPlayer() {
        synchronized (this) {
            if (started)
                return currentPlayer;
            else
                return -1;
        }
    }

    public List<CardData> getPlayerHand(UUID playerID) {
        synchronized (this) {
            return hands.get(playerID);
        }
    }

    public List<UUID> getTurnSequence() {
        synchronized (this) {
            return turnSequence;
        }
    }

    public void cancelGame() {
        synchronized (this) {
            cancelled = true;
            try {
                UserSession.retrieveSessionFromID(owner).leaveCurrentGame();
            } catch (UserSessionError userSessionError) {
                SERVER_LOGGER.warning(
                        "Unable to get game owner's session for game cancellation"
                );
            }
            removeFromList();
            propagateChange();
        }
    }

    public boolean isCancelled() {
        synchronized (this) {
            return cancelled;
        }
    }

    public void leaveGame(UUID player) {
        boolean shouldStop = false;
        synchronized (this) {
            players.remove(player);
            turnSequence.remove(player);
            if (players.size() == 0) {
               removeFromList();
            }
            if (started)
                shouldStop = true;

            if (!shouldStop)
                propagateChange();
        }

        if (shouldStop)
            stop();

    }

    public void start() throws GameException {
        synchronized (this) {
            if (players.size() < 3)
                throw new GameException("Not enough players");

            for (PlayerObject po : players.values()) {
                po.getGameData().reset();
            }

            started = true;
            shufflePlayerOrder();
            findStartingPlayer(dealCards());
            propagateChange();
            SERVER_LOGGER.info("Game started, and state propagated");
        }
    }

    public void stop() {
        synchronized (this) {
            started = false;
            currentPlayer = -1;
            passCount = 0;
            goneOut = 0;
            cardsOnTable = new ArrayList<>();
            noOfCardsFaceDown = 0;
            playersInTradingPhase = 0;
            propagateChange();
        }
    }

    private void assignRoles() {
        SERVER_LOGGER.info("Assigning roles with goneOut: " + goneOut);
        if (players.size() == 3) {
            for (PlayerObject po : players.values()) {
                if(po.getGameData().assignRoleFewPlayers())
                    playersInTradingPhase++;
            }
        } else {
            for (PlayerObject po : players.values()) {
                if(po.getGameData().assignRoleManyPlayers(players.size()))
                    playersInTradingPhase++;
            }
        }
        SERVER_LOGGER.info("Roles assigned, players in trading phase: " + playersInTradingPhase);
    }

    public void playCards(UUID player, List<CardData> cards) throws GameException {
        SERVER_LOGGER.info("Entering playCards...");
        try {
            synchronized (this) {
                if (cards.isEmpty())
                    throw new GameException("Cannot play 0 cards!");

                if (cards.get(0).getValue() != 16 && noOfCardsInTrick > 0 && noOfCardsInTrick != cards.size()) {
                    throw new GameException("Wrong number of cards");
                }

                boolean allSame = true;
                for (CardData card : cards)
                    if (card.getValue() != cards.get(0).getValue()) {   // Checks if all the cards to play are the same
                        allSame = false;
                        break;
                    }
                if(!allSame)
                    throw new GameException("All cards must have the same value");

                List<CardData> tmp = _getTopCards();

                if(!tmp.isEmpty() && tmp.get(tmp.size() - 1).getValue() > cards.get(0).getValue())
                    throw new GameException("Cards must be higher or equal to those on table");

                cardsOnTable.addAll(cards);

                SERVER_LOGGER.fine("Cards in hand before removal: " + hands.get(player).size());

                List<CardData> hand = hands.get(player);
                for (CardData card : cards) {
                    for (CardData c : hand) {
                        if (card.getSuit() == c.getSuit() && card.getNumber() == c.getNumber()) {
                            hand.remove(c);
                            break;
                        }
                    }
                }

                // setting new hand count
                PlayerData pd = players.get(player).getGameData();
                pd.setNumberOfCards(hands.get(player).size());


                // if hand is empty, go out of round
                if (hands.get(player).isEmpty()) {
                    goneOut++;
                    SERVER_LOGGER.info("Set goneout to: " + goneOut);
                    pd.setOutCount(goneOut);
                }

                // check if there is a new trick from playing
                List<CardData> topCards = _getTopCards();

                trickTriggered = Trick.NONE;
                if (cards.get(0).getValue() == 16) {
                    newTrick(Trick.THREE_CLUBS);     // 3 of clubs
                    return;
                } else if (topCards.size() == 4
                        && topCards.get(1).getValue() == topCards.get(0).getValue()
                        && topCards.get(2).getValue() == topCards.get(0).getValue()
                        && topCards.get(3).getValue() == topCards.get(0).getValue()
                ) {
                    // all 4 top cards the same, start new trick
                    newTrick(Trick.FOUR_SAME);
                    if (hands.get(player).isEmpty())
                        nextPlayer();   // player has gone out and should not get the next turn
                    return;
                }




                if (noOfCardsInTrick == 0)
                    noOfCardsInTrick = cards.size();
                nextPlayer();
                propagateChange();
            }
        } catch (RoundOver roundOver) {
            SERVER_LOGGER.info("Starting new round...");
            newRound();
        }

    }
    public void pass(UUID player) throws RoundOver {
        synchronized (this) {
            players.get(player).getGameData().setPassed(true);
            passCount++;
            nextPlayer();
            propagateChange();
        }
     }

    public void newRound() {
        synchronized (this) {
            newTrick(Trick.NONE);     // Start of game
            noOfCardsFaceDown = 0;
            roundNo++;

            assignRoles();
            dealCards();
            for (PlayerObject po : players.values()) {
                PlayerData pd = po.getGameData();
                pd.setPassed(false);
                pd.setOutOfRound(false);
            }
            currentPlayer = -1;
            goneOut = 0;
            propagateChange();
        }
    }

    public void registerGameStateChange() {
        synchronized (this) {
            propagateChange();
        }
    }

    public int getRoundNo() {
        synchronized (this) {
            return roundNo;
        }
    }

    /**
     * Private helpers
     ************************/

    private List<CardData> prepareDeck() {
        List<CardData> deck = new ArrayList<>();

        int numCards = 15;
        if (TEST_MODE)
            numCards = TEST_CARD_NUM; // to speed up testing

        char[] suits = {'H', 'S', 'C', 'D'}; // H(earts), S(pades), C(lubs), D(iamond)
        for (int suit = 0; suit < 4; suit++)        // For each suit, create 13 cards
            for (int number = 2; number < numCards; number++)
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
            SERVER_LOGGER.info("All other players have gone out. Should start new round");

            // find the last remaining player, and set outcount
            for (UUID id : hands.keySet()) {
                if (!hands.get(id).isEmpty()) {
                    players.get(id).getGameData().setOutCount(++goneOut);
                    break;
                }
            }

            throw new RoundOver();
        }

        // find the next player in turn order who hasn't passed or gone out
        PlayerData pd;
        do {

            if (++currentPlayer == players.size())
                currentPlayer = 0;
            pd = players.get(turnSequence.get(currentPlayer)).getGameData();
        } while (pd.hasPassed() || pd.isOutOfRound());

        if (passCount + goneOut == players.size()-1)
            newTrick(Trick.ALL_PASS); // Everybody says pass

    }

    // 0 indicates everybody said pass, or if it's a new round, 1 is 3 of clubs, 4 is 4 of the same card
    private void newTrick(Trick trickType) {
        for (PlayerObject po : players.values()) {
            po.getGameData().setPassed(false);
        }
        noOfCardsFaceDown += cardsOnTable.size();
        cardsOnTable = new ArrayList<>();
        noOfCardsInTrick = 0;
        passCount = 0;
        trickTriggered = trickType;
        propagateChange();
    }

    public Trick getLastTrickTriggered(){
        return trickTriggered;
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
        hands = new LinkedHashMap<>();
        for (UUID hand : turnSequence) {
            hands.put(hand, new ArrayList<>());
        }

        // deals new cards

        List<CardData> deck = prepareDeck();
        SERVER_LOGGER.info("Size of deck: " + deck.size());
        for (CardData card : deck) {

            if (player == turnSequence.size())
                player = 0;

            tmp = turnSequence.get(player++);

            List<CardData> hand = hands.get(tmp);

            if (hand != null)
                hand.add(card);
            else
                SERVER_LOGGER.warning("hand is null");

            if (playerWithThreeOfDiamonds == null
                    && card.getNumber() == 3
                    && card.getSuit() == 'D'
            ) {
                playerWithThreeOfDiamonds = tmp;
            }
        }


        // Counts all hands
        for (UUID hand : hands.keySet()) {
            players.get(hand).getGameData().setNumberOfCards(hands.get(hand).size());
        }


        return playerWithThreeOfDiamonds;
    }

    private void shufflePlayerOrder() {
        turnSequence = new ArrayList<>();
        turnSequence.addAll(players.keySet());
        Collections.shuffle(turnSequence);
        SERVER_LOGGER.fine("Shuffling player order, new sequence size: " + turnSequence.size());
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

        if(TEST_MODE)
            currentPlayer = 0; // to speed up testing

        SERVER_LOGGER.info("Set starting player to: " + currentPlayer);
    }
}
