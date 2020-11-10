package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Math.round;

public class Player extends JPanel {
    private BufferedImage image;
    private final String filePath;
    private final String name;
    private final int playerID;
    private final String role;
    private final ArrayList<Card> hand;
    private JButton removeCard;
    private JButton addCard;
    private final int cardWidth = 80;
    private final int cardHeight = 120;
    private int widthOfComponent = 500;
    int cardNumb = 18;
    int boundsX = 0;
    int cardsOnDisplay = 0;
    int internalMargin = 5;
    private int space = 24; // Space between cards when a player has maximum cards
    private final int maxCards = 18;


    public Player(String name, int playerID, String role, ArrayList<Card> cards, int width) {
        this.name = name;
        this.playerID = playerID;
        this.role = role;
        this.hand = cards;
        this.widthOfComponent = width;
        sortHand(); // Sorts the players hand with respect to the game rules

        setLayout(null);
        setOpaque(true);
        this.filePath = "./resources/green_fabric.jpg"; // Filepath
        try {
            image = ImageIO.read(new File(filePath));       // Read the image
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        addCard = new JButton("Add");
        addCard.setBounds(0,125,100,50);
        add(addCard);
        addCard.addActionListener( e -> {
            addCardToDisplay();
        });

        removeCard = new JButton("Remove");
        removeCard.setBounds(100,125,100,50);
        add(removeCard);
        removeCard.addActionListener(e -> {
            removeCardFromDisplay();
        });

        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));  // Create border
    }

    public void addCardToDisplay() {
        addAll18();
    }

    public void removeCardFromDisplay() {
        cardsOnDisplay--;
        Card temp = hand.get(++cardNumb);
        this.remove(temp);
        rearrangeCardsOnDisplay();
    }

    public void addAll18() {
        boundsX = (widthOfComponent) - (cardWidth) - internalMargin;
        int bort = 0;
        for (int i = 0; i < 18; i++) {
            Card temp = hand.get(cardNumb--);
            bort = i*space;
            temp.setBounds(boundsX - bort, 0, cardWidth, cardHeight);
            add(temp);
        }
        repaint();
        cardsOnDisplay = 18;
    }

    public void rearrangeCardsOnDisplay() {
        for (int i = 0; i < cardsOnDisplay; i++) {  // Removes all current cards
            Card temp = hand.get(cardNumb++);       // So that they can be redrawn centered and relatively spaced
            this.remove(temp);
        }

        // The spacing between cards
        space = space + ((maxCards - cardsOnDisplay)/2);
        if(cardsOnDisplay < 4)  // If the cards on the hand is less than four, don't have any space
            space = cardWidth ;

        // The x-coordinate of the first card from right to left
        boundsX = round(((float)widthOfComponent/2)  - ((float)cardWidth/2) + (((float)cardsOnDisplay-1)/2) * (float)space);
        for (int i = 0; i < cardsOnDisplay; i++) {  // For each card on hand, place them from right to left
            Card temp = hand.get(cardNumb--);       // to see the highest card first
            temp.setBounds(boundsX - (i*space) , 0, cardWidth, cardHeight);
            this.add(temp);
        }
        repaint();
    }


    // Sorts the players hand by using a quicksort
    public void sortHand() {
        QuickSort.sort(this.hand, 0, this.hand.size() - 1);
    }

    public String getName(){
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getPlayedID() {
        return Integer.toString(this.playerID);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this); // Draws the image of onto the JPanel
    }
}
