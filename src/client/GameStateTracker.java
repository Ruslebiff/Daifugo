package client;


import common.PlayerData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public interface GameStateTracker {
    boolean isMyTurn();                 // er det min tur?
    List<PlayerData> getPlayerList();   // liste over spillere, i turn order
    int getActivePlayerIndex();         // aktiv spillers index i liste
                                        //   erstatter getActivePlayerID

    void leaveGame();
    void registerCallback(Callable<Void> callback);
    List<Card> getHand(String token);   // Returns an arraylist to the player with the respective token
    int getLastPlayedType();   // Returns what was last played on the table, single, double or triple
    void passTurn();
    void setNextTurn();
    boolean playCards(List<Card> playedCards);
    boolean giveCards(List<Card> cards);
    boolean isNewTrick();
    void resetRound();
    int getNumberOfFaceDownCards();
    List<Card> getCardsOnTable();
}
