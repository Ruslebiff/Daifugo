package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

// Class for GUI of the cards played on the table
public class CardsOnTable extends JPanel implements  GameStateTracker{
    private BufferedImage image;    // Image of green felt
    private final String filePath;  // Path to image of green felt
    private final ArrayList<Card> lastFourCards = new ArrayList<>();   // The last three cards played

    public CardsOnTable(int widht, int height) {
        // Renders the green filt unto the player-section
        this.filePath = "./resources/green_fabric.jpg"; // Filepath
        try {
            image = ImageIO.read(new File(filePath));       // Read the image
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    }

    // Function shows the last three cards played on the table
    // Får kort fra server
    public void updateCardsOnTable() {
        int cardWidth = 80, cardHeight = 120;

        lastFourCards.forEach(c -> {
            c.setBounds((this.getWidth()/2) - (cardWidth/2), (this.getHeight()/2) - (cardHeight/2),
                        cardWidth, cardHeight);
            this.add(c);
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this); // Draws the image of onto the JPanel
    }

    @Override
    public String getActivePlayerID() {
        return null;
    }

    @Override
    public ArrayList<Card> dealPlayerHand(String token) {
        return null;
    }

    @Override
    public Boolean checkIfPlayable() {
        return null;
    }

    @Override
    public int getLastPlayedType() {
        return 0;
    }

    @Override
    public void relinquishTurn() {

    }

    @Override
    public void playCards(ArrayList<Card> playedCards) {

    }
}
