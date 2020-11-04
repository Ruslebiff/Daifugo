package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// client.Card class is a button representing a card on the players hand
public class Card extends JPanel {
    private final int value;          // The value of the card
    private final char suit;        // The suit of the card, i.e. diamond, spades etc.
    private final int number;
    private BufferedImage image;
    private Image scaledImage;
    private final String filePath;


    // Constructor with parameters, sets values of card
    public Card(int number, char s){


        this.number = number;
        this.suit = s;
        if(this.number == 2) {
            this.value = 15;
        } else if (this.number == 3 && this.suit  == 'C') {
            this.value = 16;
        } else {
            this.value = number;
        }

        this.filePath = "./resources/cardimages/" + this.suit + Integer.toString(this.number) + ".png"; // Filepath
        try {
            image = ImageIO.read(new File(filePath));  // Read the image
            scaledImage = image.getScaledInstance(80,120,Image.SCALE_SMOOTH);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public int getValue() {
        return value;
    }

    public int getNumber() {
        return this.number;
    }

    public char getSuit() {
        return suit;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(scaledImage, 0, 0, this); // Draws the image of onto the Jpanel
    }
}

