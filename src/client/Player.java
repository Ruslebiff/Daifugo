package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.round;

public class Player extends JPanel {
    private BufferedImage image;    // Image of green felt
    private final String filePath;  // Path to image of green felt
    private final String name;      // Name of player
    private final int playerID;     // Id
    private final String role;      // Role, i.e. president, vice president etc.
    private final ArrayList<Card> hand; // The cards dealt to the player
    private JButton removeCard;
    private JButton addCard;
    private final PlayerButton playCards;   // Button plays the selected cards
    private final PlayerButton cancelPlay;
    private final PlayerButton passTurn;
    private final int cardWidth = 80;
    private final int cardHeight = 120;
    private final int widthOfComponent;
    private final int buttonWidth = 100;
    private final int buttonHeight = 50;
    private int boundsX = 0;
    private int space = 24; // Space between cards when a player has maximum cards
    private final int maxCards = 18;
    private boolean myTurn = true;


    public Player(String name, int playerID, String role, ArrayList<Card> cards, int width) {
        this.name = name;
        this.playerID = playerID;
        this.role = role;
        this.hand = cards;
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
        playCards.addActionListener(e -> playSelectedCards());
        add(playCards);

        cancelPlay = new PlayerButton((widthOfComponent/3)-(buttonWidth/2) + playCards.getBounds().x, 0,
                                        buttonWidth, buttonHeight, "Cancel");
        cancelPlay.addActionListener(e -> cancel());
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
        if(myTurn) {
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
    }


    public void addCardToDisplay() {
        viewDealtHand();
    }

    public void removeCardFromDisplay() {
        this.remove(hand.get(0));
        hand.remove(0);
        rearrangeCardsOnDisplay();
    }

    // TODO: Y-coordinate must be further down

    public void viewDealtHand() {
        space = space + ((maxCards - hand.size())/2);
        boundsX = (widthOfComponent/2) - (cardWidth/2) + (((hand.size()-1)/2) * space);
        int i = 0;
        for (Card card: hand) {
            card.setBounds(boundsX - ((i++)*space), 50, cardWidth, cardHeight);
            add(card);
        }
        repaint();
    }

    public void rearrangeCardsOnDisplay() {
        hand.forEach(this::remove);

        // The spacing between cards
        space = space + ((maxCards - hand.size())/2);
        System.out.println("SPACE " + space);
        if(hand.size() < 4)  // If the cards on the hand is less than four, don't create any space
            space = cardWidth;
        System.out.println("SPACE AFTER HAND.SIZE CHECK " + space);

        // TODO: Y-coordinate must be further down
        // The x-coordinate of the first card from right to left
        boundsX = round(((float)widthOfComponent/2)  - ((float)cardWidth/2) + (((float)hand.size()-1)/2) * (float)space);
        int i = 0;
        for (Card card: hand) {
            card.setBounds(boundsX - ((i++)*space), 50, cardWidth, cardHeight);
            add(card);
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


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this); // Draws the image of onto the JPanel
    }

    public void cancel() {
        hand.forEach(c -> {
            c.setSelectedFalse();   // Deselects all the cards
            c.repaint();

        });
    }

    public void playSelectedCards() {
        // Server.playCards()
        // if selected cards are ok to play
        // if server-response was gucci, remove the cards from the hand, sort the hand again
        ArrayList<Card> cardsToPlay = new ArrayList<>();
        hand.forEach(c -> {
            if(c.isSelected()) {
                cardsToPlay.add(c);
            }
        });

        // If cards to play is allowed, to this
        cardsToPlay.forEach(c -> {
            hand.remove(c);
            this.remove(c); 
        });
        
        sortHand();
        rearrangeCardsOnDisplay();

    }



}
