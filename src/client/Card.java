package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// client.Card class is a button representing a card on the players hand
public class Card extends JPanel {
    private final int value;        // The value of the card in terms of the game rules
    private final char suit;        // The suit of the card, i.e. diamond, spades etc.
    private final int number;       // The actual value on the card
    private BufferedImage image;
    private Image scaledImage;
    private final String filePath;
    private int cardWidth = 80;
    private int cardHeight = 120;
    private boolean isSelected = false;



    // Constructor with parameters, sets values of card
    public Card(int number, char s){

        setOpaque(false);
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
            scaledImage = image.getScaledInstance(cardWidth,cardHeight,Image.SCALE_SMOOTH);
        } catch (IOException ex) {
            ex.printStackTrace();
        }


//        addListener();
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

    @Override       // If the card is not selected -> paint normally, else paint with overlay
    protected void paintComponent(Graphics g) {
        if(!isSelected) {
            super.paintComponent(g);       // Rounds the corner of the cards
            RoundRectangle2D r = new RoundRectangle2D.Float(0, 0, cardWidth, cardHeight, 10, 10);
            g.setClip(r);
            g.drawImage(scaledImage, 0, 0, this); // Draws the image of onto the Jpanel
        }
        else
            paintOverlay(g);
    }

    // Paints the component with a transparent overlay
    protected void paintOverlay(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.drawImage(scaledImage, 0, 0, this);
        g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
        g2d.setColor(new Color(135,206,250));
        g2d.fillRect(0, 0, cardWidth, cardHeight);
        g2d.dispose();
    }

    public void setSelected(){
        isSelected = !isSelected;
    }
}

