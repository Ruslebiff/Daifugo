package client;


import java.util.ArrayList;
import java.util.Collections;

public interface GameStateTracker {
    String getActivePlayerID(); // returns nick or unique identifier of current player
    ArrayList<Card> dealPlayerHand(String token);   // Returns an arraylist to the player with the respective token
    Boolean checkIfPlayable();
    int getLastPlayedType();   // Returns what was last played on the table, single, double or triple
    void relinquishTurn();
    void playCards(ArrayList<Card> playedCards);

    /**
     * Function populates the deck of cards 'cardList' and shuffles it
     * @return Array list of cards
     */
    default ArrayList<Card> getDeck() {
        ArrayList<Card> cardList = new ArrayList<>();
        char[] suits = {'H', 'S', 'C', 'D'}; // H(earts), S(pades), C(lubs), D(iamond)
        for (int suit = 0; suit < 4; suit++)        // For each suit, create 13 cards
            for (int number = 2; number < 15; number++)
                cardList.add(new Card(number, suits[suit]));    // Add the card to the cardList

        Collections.shuffle(cardList);          // Shuffle the cards
        return cardList;
    }
}
