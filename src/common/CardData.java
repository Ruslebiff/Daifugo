package common;

import java.io.Serializable;

public class CardData implements Serializable {
    private final int number;
    private final int value;
    private final char suit;

    public CardData(int number, char suit) {
        this.number = number;
        this.suit = suit;

        if(this.number == 2) {
            this.value = 15;
        } else if (this.number == 3 && this.suit  == 'C') {
            this.value = 16;
        } else {
            this.value = number;
        }
    }

    public char getSuit() {
        return suit;
    }

    public int getNumber() {
        return number;
    }

    public int getValue() {
        return value;
    }
}
