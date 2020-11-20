package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static client.GameLobby.LOGGER;

// Class for GUI of the cards played on the table
public class CardsOnTable extends JPanel{
    private BufferedImage image;    // Image of green felt
    private List<Card> lastCardsOnTable = new ArrayList<>();   // The last three cards played
    private List<FaceDownCard> faceDownCards = new ArrayList<>();   // stack of cards facing down
    private final GameStateTracker stateTracker;


    public CardsOnTable(GameStateTracker sT, int panelWidth, int panelHeight) {
        setLayout(null);
        setOpaque(false);
        stateTracker = sT;

        try {
            image = ImageIO.read(  // Renders the green felt
                    ClientMain.class.getResourceAsStream("/green_fabric.jpg") // Read the image
            );
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    // Function shows the last cards played on the table
    public void updateCardsOnTable() {
        for (Card card : lastCardsOnTable) {
            this.remove(card);
        }

        // clear board at new round
        if (stateTracker.getNumberOfFaceDownCards() == 0) {
            for(FaceDownCard fc : faceDownCards) {
                this.remove(fc);
            }
            faceDownCards = new ArrayList<>(); // reset
        }

        int cardWidth = 80, cardHeight = 120;
        lastCardsOnTable = stateTracker.getCardsOnTable();


        for (int i = lastCardsOnTable.size() - 1; i >= 0 ; i--) {
            Card c = lastCardsOnTable.get(i);
            c.setBounds(
                    (this.getWidth()/2) + (i*10),
                    (this.getHeight()/2) - (cardHeight/2),
                    cardWidth,
                    cardHeight
            );
            this.add(c);
        }

        int noOfFaceDown = stateTracker.getNumberOfFaceDownCards();
        if (noOfFaceDown > 10){ // Set max face down cards
            noOfFaceDown = 10;
        }
        if (noOfFaceDown > faceDownCards.size()){
            for (int i = 0; i < noOfFaceDown; i++) {
                int boundsX = 1 + i;
                if(boundsX > 10)
                    boundsX = 0;
                FaceDownCard tmp = new FaceDownCard();
                faceDownCards.add(tmp);
                tmp.setBounds(boundsX, (this.getHeight()/2) - (cardHeight/2), cardWidth, cardHeight);
                add(tmp);
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //g.drawImage(image, 0, 0, this); // Draws the image of onto the JPanel
    }
}
