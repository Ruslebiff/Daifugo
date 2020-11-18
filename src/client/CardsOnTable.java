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
    private JLabel cardsInPlayCounter;

    public CardsOnTable(int width, int height, GameStateTracker sT) {
        faceDownCards = 0;
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

        cardsInPlayCounter = new JLabel();
        cardsInPlayCounter.setBounds((this.getWidth()/2) - 25 ,(this.getHeight()/2) - 25,50,50);
        cardsInPlayCounter.setFont(new Font("Sans Serif", Font.BOLD, 20));
        add(cardsInPlayCounter);
    }

    // Function shows the last cards played on the table
    public void updateCardsOnTable() {
        for (Card card : lastCardsOnTable) {
            this.remove(card);
        }

        int cardWidth = 80, cardHeight = 120;
        lastCardsOnTable = stateTracker.getCardsOnTable();
        LOGGER.info("Top cards size: " + lastCardsOnTable.size());

        cardsInPlayCounter.setVisible(!lastCardsOnTable.isEmpty());     // How many cards are on the table
        cardsInPlayCounter.setText(Integer.toString(lastCardsOnTable.size()));

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
        LOGGER.info("Antall facedown " + faceDownCards);
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
