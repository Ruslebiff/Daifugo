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
    private int faceDownCards;
    private final GameStateTracker stateTracker;
    private BufferedImage faceDown;

    public CardsOnTable(int width, int height, GameStateTracker sT) {
        stateTracker = sT;
        try {
            image = ImageIO.read(  // Renders the green filt
                    ClientMain.class.getResourceAsStream("/green_fabric.jpg"));       // Read the image

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        try {
            faceDown = ImageIO.read(  // Renders the green filt
                    ClientMain.class.getResourceAsStream("/cardimages/Daifugo_cardback_fade_blue_vertical.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Function shows the last cards played on the table
    public void updateCardsOnTable() {
        for (Card card : lastCardsOnTable) {
            this.remove(card);
        }

        int cardWidth = 80, cardHeight = 120;
        lastCardsOnTable = stateTracker.getCardsOnTable();
//        lastCardsOnTable.forEach(c -> {
//            c.setBounds(
//                    (this.getWidth()/2) - (cardWidth/2),
//                    (this.getHeight()/2) - (cardHeight/2),
//                    cardWidth,
//                    cardHeight
//
//            );
//            this.add(c);
//            c.repaint();
//        });



        for (int i = lastCardsOnTable.size() - 1; i >= 0 ; i--) {
            Card c = lastCardsOnTable.get(i);
            c.setBounds(
                    (this.getWidth()/2) - (cardWidth/2) + (i*10),
                    (this.getHeight()/2) - (cardHeight/2),
                    cardWidth,
                    cardHeight

            );
            this.add(c);
        }




        faceDownCards = stateTracker.getNumberOfFaceDownCards();
        for (int i = 0; i < faceDownCards; i++) {
            FaceDownCard tmp = new FaceDownCard();
            tmp.setBounds(i, (this.getHeight()/2) - (cardHeight/2), cardWidth, cardHeight);
            add(tmp);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this); // Draws the image of onto the JPanel
    }
}
