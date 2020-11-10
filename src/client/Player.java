package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
    private final PlayerButton playCards;
    private final PlayerButton cancelPlay;
    private final PlayerButton passTurn;
    private final int cardWidth = 80;
    private final int cardHeight = 120;
    private final int widthOfComponent;
    private final int buttonWidth = 100;
    private final int buttonHeight = 50;
    private int cardIndex;
    private int boundsX = 0;
    private int cardsOnDisplay = 0;
    private int space = 24; // Space between cards when a player has maximum cards
    private final int maxCards = 18;


    public Player(String name, int playerID, String role, ArrayList<Card> cards, int width) {
        this.name = name;
        this.playerID = playerID;
        this.role = role;
        this.hand = cards;
        this.cardIndex = hand.size() - 1;
        this.widthOfComponent = width;
        sortHand(); // Sorts the players hand with respect to the card values
        System.out.println("Hand size " + hand.size());
        setLayout(null);
        setOpaque(true);
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));  // Create border

        // Renders the green filt unto the player-section
        this.filePath = "./resources/green_fabric.jpg"; // Filepath
        try {
            image = ImageIO.read(new File(filePath));       // Read the image
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        hand.forEach(this::addListener);    // Adds a mouseListener for each card

        passTurn = new PlayerButton((widthOfComponent/3)-(buttonWidth) + 15, 0, // TODO: Give up turn
                                        buttonWidth, buttonHeight, "Pass Turn");
        add(passTurn);
                                // TODO: Check if selected cards are "legal" to play, if yes -> play on table, else put back in hand/display
        playCards = new PlayerButton((widthOfComponent/3)-(buttonWidth/2) + passTurn.getBounds().x, 0,
                                        buttonWidth, buttonHeight, "Play Cards");
        add(playCards);

        cancelPlay = new PlayerButton((widthOfComponent/3)-(buttonWidth/2) + playCards.getBounds().x, 0,
                                        buttonWidth, buttonHeight, "Cancel");
        cancelPlay.addActionListener(e -> { cancel();});
        add(cancelPlay);


        // TODO: REMOVE ADD AND REMOVE BUTTONS
        addCard = new JButton("Add");
        addCard.setBounds(0,175,buttonWidth,buttonHeight);
        add(addCard);
        addCard.addActionListener( e -> {
            addCardToDisplay();
        });

        removeCard = new JButton("Remove");
        removeCard.setBounds(100,175,100,50);
        add(removeCard);
        removeCard.addActionListener(e -> {
            removeCardFromDisplay();
        });
    }

    public void addListener(Card c) {
        c.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {    // Upon selection, paint/unpaint the component with overlay
                c.setSelected();
                c.paintComponent(c.getGraphics());
                hand.forEach(c -> repaint());
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }


    public void addCardToDisplay() {
        viewDealtHand();
    }

    public void removeCardFromDisplay() {
        cardsOnDisplay--;
        Card temp = hand.get(++cardIndex);
        this.remove(temp);
        rearrangeCardsOnDisplay();
    }

    // TODO: Y-coordinate must be further down

    public void viewDealtHand() {
        space = space + ((maxCards - hand.size())/2);
        boundsX = (widthOfComponent/2) - (cardWidth/2) + (((hand.size()-1)/2) * space);
        for (int i = 0; i < hand.size(); i++) {
            Card temp = hand.get(cardIndex--);
            temp.setBounds(boundsX - (i*space), 50, cardWidth, cardHeight);
            add(temp);
        }
        repaint();
        cardsOnDisplay =  hand.size();
    }

    public void rearrangeCardsOnDisplay() {
        for (int i = 0; i < cardsOnDisplay; i++) {  // Removes all current cards
            Card temp = hand.get(cardIndex++);       // So that they can be redrawn centered and relatively spaced
            this.remove(temp);
        }

        // The spacing between cards
        space = space + ((maxCards - cardsOnDisplay)/2);
        if(cardsOnDisplay < 4)  // If the cards on the hand is less than four, don't have any space
            space = cardWidth;


        // TODO: Y-coordinate must be further down
        // The x-coordinate of the first card from right to left
        boundsX = round(((float)widthOfComponent/2)  - ((float)cardWidth/2) + (((float)cardsOnDisplay-1)/2) * (float)space);
        for (int i = 0; i < cardsOnDisplay; i++) {  // For each card on hand, place them from right to left
            Card temp = hand.get(cardIndex--);       // to see the highest card first
            temp.setBounds(boundsX - (i*space) , 50, cardWidth, cardHeight);
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

    public Card[] playCards(Card[] selectedCards) {
        return null;
    }

    public void cancel() {
        hand.forEach(c -> {
            c.setSelectedFalse();
            c.repaint();
        });
    }
}
