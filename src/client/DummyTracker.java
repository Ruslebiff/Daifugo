package client;

import java.util.ArrayList;
import java.util.Collections;

public class DummyTracker implements GameStateTracker{
    private ArrayList<Card> deck = new ArrayList<>();
    private ArrayList<Card> p1_cards = new ArrayList<>();   // TODO: REMOVE
    private ArrayList<Card> playedCards = new ArrayList<>();    // array list of the cards played
    private int playedType = 0;     // integer indicating if the cards played are singles, doubles or triples

    public DummyTracker(){

        char[] suits = {'H', 'S', 'C', 'D'}; // H(earts), S(pades), C(lubs), D(iamond)
        for (int suit = 0; suit < 4; suit++)        // For each suit, create 13 cards
            for (int number = 2; number < 15; number++)
                deck.add(new Card(number, suits[suit]));    // Add the card to the cardList
        Collections.shuffle(deck);          // Shuffle the cards

        for (int i = 0; i < deck.size()/3; i++) {
            p1_cards.add(deck.get(i));      // TODO: Change later
        }
    }

    @Override
    public String getActivePlayerID() {
        return "0"; // TODO: Change
    }

    @Override
    public ArrayList<Card> getHand(String token) {
        return p1_cards; // TODO: return the hand corresponding to the player
    }

    @Override
    public int getLastPlayedType() {
        return playedType;
    }

    @Override
    public void relinquishTurn() {

    }

    @Override
    public void setNextTurn() {
        // TODO: set the turn of next player
    }

    @Override
    public boolean playCards(ArrayList<Card> pC) {
        playedType = pC.size();
        this.playedCards.addAll(pC);
        return true;
    }

    @Override
    public boolean giveCards(ArrayList<Card> cards, int role) {
        // TODO: Give cards to the player with role Math.Abs(role)
        return true;
    }

    @Override
    public ArrayList<Card> getLastPlayedCards() {
        return playedCards;
    }
}
