package client;

import server.Game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

// Class for GUI of the cards played on the table
public class CardsOnTable extends JPanel{
    private BufferedImage image;    // Image of green felt
    private ArrayList<Card> lastFourCards = new ArrayList<>();   // The last three cards played
    private final GameStateTracker stateTracker;

    public CardsOnTable(int widht, int height, GameStateTracker sT) {
        stateTracker = sT;
        try {
            image = ImageIO.read(  // Renders the green filt
                    ClientMain.class.getResourceAsStream("/green_fabric.jpg"));       // Read the image
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    }

    // Function shows the last cards played on the table
    public void updateCardsOnTable() {
        int cardWidth = 80, cardHeight = 120;
        lastFourCards = stateTracker.getLastPlayedCards();
        lastFourCards.forEach(c -> {
            c.setBounds(
                    (this.getWidth()/2) - (cardWidth/2),
                    (this.getHeight()/2) - (cardHeight/2),
                    cardWidth,
                    cardHeight
            );
            this.add(c);
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this); // Draws the image of onto the JPanel
    }
}
