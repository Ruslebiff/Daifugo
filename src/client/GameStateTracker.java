package client;


import common.PlayerData;
import common.Role;
import common.Trick;

import java.util.List;
import java.util.concurrent.Callable;

public interface GameStateTracker {
    boolean isOwner();
    boolean isMyTurn();                 // er det min tur?
    List<PlayerData> getPlayerList();   // liste over spillere, i turn order
    int getActivePlayerIndex();         // aktiv spillers index i liste

    boolean isCancelled();
    void cancelGame();
    void leaveGame();
    void registerCallback(Callable<Void> callback);
    List<Card> getHand();   // Returns an arraylist to the player with the respective token
    void passTurn();
    boolean playCards(List<Card> playedCards);
    boolean giveCards(List<Card> cards);
    int getNumberOfFaceDownCards();
    List<Card> getCardsOnTable();
    boolean startGame();
    boolean stopGame();
    boolean isStarted();
    int getCardsInTrick();
    int getRoundNo();
    boolean isTradingPhase();
    boolean iHaveToTrade();
    int getMyRoleNumber();
    void registerConnectionLostCallback(Callable<Void> func);
    Role getRole();
    int getCardsInPlay();
    boolean isNewTrick();
    Trick getLastTrick();
    //boolean haveGoneOut();
    int getGoneOutNumber();
}
