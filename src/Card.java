import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// Card class is a button representing a card on the players hand
public class Card extends JPanel {
    private final int value;          // The value of the card
    private final char suit;        // The suit of the card, i.e. diamond, spades etc.
    private BufferedImage image;
    private final String filePath;

    // Constructor with parameters, sets values of card
    public Card(int val, char srt){
        this.value = val;
        this.suit = srt;
        this.filePath = "./src/images/" + this.suit + Integer.toString(this.value); // Filepath
        try {
            image = ImageIO.read(new File(filePath));       // Read the image
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public int getValue() {
        return value;
    }

    public char getSuit() {
        return suit;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this); // Draws the image of onto the Jpanel
    }
}

