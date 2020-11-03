import javax.swing.*;
import java.io.File;

// Card class is a button representing a card on the players hand
public class Card extends JButton {
    private final int value;          // The value of the card
    private final String sort;        // The sort of the card, i.e. diamond, spades etc.
    private final Icon cardImage;     // The image of the card

    public Card(int val, String srt, String filePath){
        this.value = val;
        this.sort = srt;
        this.cardImage = new ImageIcon(String.valueOf(new File(filePath)));
        setIcon(cardImage);
    }

    public int getValue() {
        return value;
    }

    public String getSort() {
        return sort;
    }
}