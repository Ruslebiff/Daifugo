package client;


import common.PlayerData;

import java.util.ArrayList;
import java.util.List;

public interface GameStateTracker {
    String getActivePlayerID(); // returns nick or unique identifier of current player
    ArrayList<Card> getHand(String token);   // Returns an arraylist to the player with the respective token
    int getLastPlayedType();   // Returns what was last played on the table, single, double or triple
    void relinquishTurn();
    void setNextTurn();
    boolean playCards(ArrayList<Card> playedCards);
    boolean giveCards(ArrayList<Card> cards, int role);
    ArrayList<Card> getLastPlayedCards();
}
