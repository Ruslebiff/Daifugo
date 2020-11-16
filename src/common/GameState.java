package common;

import server.*;
import server.exceptions.*;

import java.io.Serializable;
import java.util.*;

public class GameState implements Serializable {
    private ArrayList<PlayerData> players;
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


    public GameState(Game game, UserSession session) throws UserSessionError {

        this.playerNick = session.getNick();
        this.ownerNick = game.getOwnerNick();
        this.gameTitle = game.getTitle();
        this.cardsInTrick = game.getNoOfCardsInTrick();
        this.topCards = game.getTopCards();
        gameID = game.getID();

        if (!game.hasStarted())
            currentPlayer = -1;
        else
            currentPlayer = game.getCurrentPlayer();

        hand = game.getPlayerHand(session.getID());

        Map<UUID, PlayerObject> playerMap = game.getPlayers();
        players = new ArrayList<>();
        for (UUID id : game.getTurnSequence()) {
            players.add(playerMap.get(id).getGameData());
        }

        started = game.hasStarted();
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
        return players.get(currentPlayer).getNick().equals(playerNick);
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

    public List<CardData> getHand() { return hand; }

    public ArrayList<PlayerData> getPlayers() {
        return players;
    }

}
