package client;

import java.util.ArrayList;

public interface GameStateTracker {
    String getActivePlayerID(); // returns nick or unique identifier of current player
    ArrayList<Card> getPlayerHand(String token);   // Returns an arraylist to the player with the respective token
}
