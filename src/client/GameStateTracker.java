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

    void registerCallback(Callable<Void> callback);
    String getActivePlayerID();
    ArrayList<Card> getHand(String token);   // Returns an arraylist to the player with the respective token
    int getLastPlayedType();   // Returns what was last played on the table, single, double or triple
    void relinquishTurn();
    void setNextTurn();
    boolean getIsMyTurn();
    boolean playCards(ArrayList<Card> playedCards);
    boolean giveCards(ArrayList<Card> cards, int role);
    boolean isNewTrick();
    ArrayList<Card> getLastPlayedCards();
    ArrayList<Card> getRoundCards();
    void resetRound();
    void setAllFourSameCards();
}
