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
    private List<FaceDownCard> faceDownCards = new ArrayList<>();   // The last three cards played
    private final GameStateTracker stateTracker;
    private final JLabel cardsInPlayCounter;

    public CardsOnTable(GameStateTracker sT, int panelWidth, int panelHeight) {
        setLayout(null);
        stateTracker = sT;
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        try {
            image = ImageIO.read(  // Renders the green felt
                    ClientMain.class.getResourceAsStream("/green_fabric.jpg") // Read the image
            );
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        cardsInPlayCounter = new JLabel();
        cardsInPlayCounter.setBounds(panelWidth - 50, (panelHeight/2) - 25,50,50);
        cardsInPlayCounter.setFont(new Font("Sans Serif", Font.BOLD, 20));
        add(cardsInPlayCounter);
    }

    // Function shows the last cards played on the table
    public void updateCardsOnTable() {
        for (Card card : lastCardsOnTable) {
            this.remove(card);
        }
        for(FaceDownCard fc : faceDownCards) {
            this.remove(fc);
        }

        int cardWidth = 80, cardHeight = 120;
        lastCardsOnTable = stateTracker.getCardsOnTable();

        cardsInPlayCounter.setVisible(stateTracker.getCardsInPlay() != 0);
        cardsInPlayCounter.setText(Integer.toString(stateTracker.getCardsInPlay()));


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

        int noOfFaceDown = stateTracker.getNumberOfFaceDownCards();
        for (int i = 0; i < noOfFaceDown; i++) {
            int boundsX = 1 + i;
            if(i > 5)
                boundsX -= 5;
            FaceDownCard tmp = new FaceDownCard();
            faceDownCards.add(tmp);
            tmp.setBounds(boundsX, (this.getHeight()/2) - (cardHeight/2), cardWidth, cardHeight);
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
