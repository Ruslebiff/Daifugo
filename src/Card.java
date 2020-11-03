import javax.swing.*;

// Card class is a button representing a card on the players hand
public class Card extends JButton {
    private final int value;          // The value of the card
    private final char suit;        // The suit of the card, i.e. diamond, spades etc.

    // Constructor with parameters, sets values of card
    public Card(int val, char srt, Icon cardImage){
        this.value = val;
        this.suit = srt;
        setIcon(cardImage);
    }

    public int getValue() {
        return value;
    }

    public char getSuit() {
        return suit;
    }
}

