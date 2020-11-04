package client;

import java.util.ArrayList;
import java.util.Collections;

public interface GameStateTracker {
    ArrayList<Card> cardList = new ArrayList<>();

    String getActivePlayerID(); // returns nick or unique identifier of current player
    ArrayList<Card> dealPlayerHand(String token);   // Returns an arraylist to the player with the respective token

    /**
     * Function populates the deck of cards 'cardList' and shuffles it
     * @return Array list of cards
     */
    default ArrayList<Card> getDeck() {
        char[] suits = {'H', 'S', 'C', 'D'}; // H(earts), S(pades), C(lubs), D(iamond)
        for (int suit = 0; suit < 4; suit++)        // For each suit, create 13 cards
            for (int number = 2; number < 15; number++)
                cardList.add(new Card(number, suits[suit]));    // Add the card to the cardList

        Collections.shuffle(cardList);          // Shuffle the cards

        return cardList;
    }

}
