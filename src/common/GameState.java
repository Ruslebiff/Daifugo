package common;

import server.*;
import server.exceptions.*;

import java.io.Serializable;
import java.util.*;

import static server.Server.SERVER_LOGGER;

public class GameState implements Serializable {
    private List<PlayerData> players;
    private List<CardData> hand;

    // replication of general game data
    private boolean started;
    private int currentPlayer;  // will be negative if game isn't started
    private UUID gameID;
    private String gameTitle;
    private String ownerNick;
    private String playerNick;
    private int cardsInTrick;
    private int faceDownCards;
    private List<CardData> topCards;
    private boolean tradingPhase;
    private int roundNo;
    private boolean mustTrade;
    private Role role;


    public GameState(Game game, UserSession session) throws UserSessionError {

        this.playerNick = session.getNick();
        this.ownerNick = game.getOwnerNick();
        this.gameTitle = game.getTitle();
        this.cardsInTrick = game.getNoOfCardsInTrick();
        this.topCards = game.getTopCards();

        roundNo = game.getRoundNo();
        tradingPhase = game.isTradingPhase();
        gameID = game.getID();

        currentPlayer = game.getCurrentPlayer();

        try {
            hand = new ArrayList<>(game.getPlayerHand(session.getID()));
        } catch (Exception ignored) {
            hand = new ArrayList<>();
        }
        if (!hand.isEmpty())
            for (CardData c : hand) {
                SERVER_LOGGER.fine("added to " + playerNick + "'s hand for sending: " + c.getNumber()+c.getSuit());
            }

        Map<UUID, PlayerObject> playerMap = game.getPlayers();
        players = new ArrayList<>();
        for (UUID id : game.getTurnSequence()) {
            players.add(playerMap.get(id).getGameData());
        }

        started = game.hasStarted();

        mustTrade = playerMap.get(session.getID()).getGameData().hasToTrade();
        role = playerMap.get(session.getID()).getGameData().getRole();
    }

    public Role getRole() {
        return role;
    }

    public boolean haveToTrade() {
        return mustTrade;
    }

    public int getRoundNo() {
        return roundNo;
    }

    public boolean isTradingPhase() {
        return tradingPhase;
    }

    public List<CardData> getTopCards() {
        return topCards;
    }

    public int getCardsInTrick() {
        return cardsInTrick;
    }

    public int getFaceDownCards() {
        return faceDownCards;
    }

    public boolean isMyTurn() {
        if (currentPlayer < 0)
            return false;
        else
            return players.get(currentPlayer).getNick().equals(playerNick);
    }

    public boolean iAmOwner() {
        return ownerNick.equals(playerNick);
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public String getGameID() {
        return gameID.toString();
    }

    public boolean isStarted() {
        return started;
    }

    public List<CardData> getHand() {
        return new ArrayList<>(hand);
    }

    public List<PlayerData> getPlayers() {
        return players;
    }

}
