package client;

import client.networking.ClientConnection;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.round;

public class Player extends JPanel{
    private final GameStateTracker stateTracker;
    private BufferedImage image;    // Image of green felt
    private final int role;      // Role, -2 = Bum, -1 = ViceBum, 0 = Neutral, 1 = VP, 2 = President
    private List<Card> hand ; // The cards dealt to the player
    private final List<Card> cardsToPlay = new ArrayList<>();
    private JButton removeCard;

    // TODO: REMOVE ADD AND REMOVE BUTTONS
    private final PlayerButton playCardsBtn;   // Button plays the selected cards
    private final PlayerButton cancelBtn;
    private final PlayerButton passTurnBtn;
    private final int cardWidth = 80;
    private final int cardHeight = 120;
    private final int widthOfComponent;
    private int boundsX = 0;
    private int space = 24; // Space between cards when a player has maximum cards
    private final int maxCards = 18;
    private boolean giveCards = false; // Is set by server
    private boolean cardsClickable = true;


    public Player(int width, GameStateTracker sT) {
        this.stateTracker = sT;
        this.role = 0; // Upon creation of a player, the player will be set to neutral, as the game has just begun
        widthOfComponent = width;
        setLayout(null);
        setOpaque(true);
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));  // Create border

        // Renders the green felt unto the player-section
        try {
            image = ImageIO.read(
                    ClientMain.class.getResourceAsStream("/green_fabric.jpg")
            );        // Read the image

        } catch (IOException ex) {
            ex.printStackTrace();
        }


        int buttonWidth = 100;
        int buttonHeight = 50;
        passTurnBtn = new PlayerButton((widthOfComponent / 3) - (buttonWidth) + 15, 0, // Relinquishes turn
                buttonWidth, buttonHeight, "Pass Turn");
        passTurnBtn.addActionListener(e -> relinquishTurn());
        add(passTurnBtn);

        playCardsBtn = new PlayerButton((widthOfComponent / 3) - (buttonWidth / 2) + passTurnBtn.getBounds().x, 0,
                buttonWidth, buttonHeight, "Play Cards");
        playCardsBtn.setEnabled(false);


        cancelBtn = new PlayerButton((widthOfComponent / 3) - (buttonWidth / 2) + playCardsBtn.getBounds().x, 0,
                buttonWidth, buttonHeight, "Cancel");
        cancelBtn.addActionListener(e -> cancel());
        add(cancelBtn);

        playCardsBtn.addActionListener(e -> {
            playCards();
            cancelBtn.setEnabled(true);
            passTurnBtn.setEnabled(true);
        });
        add(playCardsBtn);



        removeCard = new JButton("Remove");
        removeCard.setBounds(100, 175, 100, 50);
        add(removeCard);
        removeCard.addActionListener(e -> removeCardFromDisplay());

    }

    public void update() {
        hand = stateTracker.getHand();
        sortHand(); // Sorts the players hand with respect to the card values
        hand.forEach(this::addListener);    // Adds a mouseListener for each card
        viewDealtHand();
    }

    public void addListener(Card c) {

        c.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {    // Upon selection, paint/unpaint the component with overlay
                if (stateTracker.isMyTurn() && cardsClickable) {
                    c.setSelected();    // Set the object to either selected or not, based upon what it previously was
                    c.paintComponent(c.getGraphics());  // Paint it accordingly
                    hand.forEach(c -> repaint());       // Repaint all the rest of the cards

                    if (c.isSelected())      // If the card is selected, add it to the arrayList
                        cardsToPlay.add(c);
                    else
                        cardsToPlay.remove(c);

                    if (!giveCards)
                        playCardsBtn.setEnabled(checkIfPlayable());
                    else // If the round has just started and you have to relinquish cards
                        giveUpCards();  // Function checks role and cards you have to give based on role

                    playCardsBtn.setEnabled(checkIfPlayable());
                }
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

    public void removeCardFromDisplay() {
        this.remove(hand.get(0));
        hand.remove(0);
        rearrangeCardsOnDisplay();
    }

    // TODO: Y-coordinate must be further down

    // TODO: når eier trykker startGame, bruk funksjon
    public void viewDealtHand() {
        space = space + ((maxCards - hand.size()) / 2);
        boundsX = (widthOfComponent / 2) - (cardWidth / 2) + (((hand.size() - 1) / 2) * space);
        int i = 0;
        for (Card card : hand) {
            card.setBounds(boundsX - ((i++) * space), 50, cardWidth, cardHeight);
            add(card);
        }
        repaint();
    }

    public void rearrangeCardsOnDisplay() {
        hand.forEach(this::remove); // Remove all the cards on the GUI to repaint them centered
        // The spacing between cards
        space = space + ((maxCards - hand.size()) / 2);

        if (hand.size() < 4)  // If the cards on the hand is less than four, don't create any space
            space = cardWidth;

        // TODO: Y-coordinate must be further down
        // The x-coordinate of the first card from right to left
        boundsX = round(((float) widthOfComponent / 2) - ((float) cardWidth / 2) + (((float) hand.size() - 1) / 2) * (float) space);
        int i = 0;
        for (Card card : hand) {     // For each card, space it more and more to the left
            card.setBounds(boundsX - ((i++) * space), 50, cardWidth, cardHeight);
            add(card);  // Add the cards again with the updated coordinates
        }
        repaint();
    }

    // Sorts the players hand by using a quicksort
    public void sortHand() {
        QuickSort.sort(this.hand, 0, this.hand.size() - 1);
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

    // Function removes cards from hand and GUI and sorts the remaining cards
    public void playCards() {
        if(!giveCards) {   // If it is a normal round, play the cards
            boolean ok;
            do {
                ok = stateTracker.playCards(cardsToPlay);
            } while (!ok);
        } else {            // If it is at the beginning of the round
            playCardsBtn.setText("Play Cards");
            giveCards = false;
            cardsClickable = true;
            stateTracker.giveCards(cardsToPlay);   // Give cards to player
        }

        // Clean up the buffer of cards to play
        cardsToPlay.forEach(c -> {
            hand.remove(c);     // Remove them from hand
            this.remove(c);     // Remove them from GUI
        });
        if (hand.size() != 0) {   // If player has more cards left
            sortHand();             // Sort hand accordingly
            rearrangeCardsOnDisplay(); // Display properly
        }
        cardsToPlay.removeAll(cardsToPlay); // Remove all cards to play from cards to play
    }

    public Boolean checkIfPlayable() {
        if(cardsToPlay.size() != 0) {   // If the player has selected cards
            List<Card> lastPlayed = stateTracker.getCardsOnTable(); // Last played cards

            if (cardsToPlay.size() == 1 && cardsToPlay.get(0).getValue() == 16) // If player selected 3 of clubs
                return true;

            // Check that the amount of selected cards is the same as the last played cards
            if (cardsToPlay.size() == lastPlayed.size()) {
                boolean allSame = false;
                for (Card card : cardsToPlay) {
                    if (card.getValue() == cardsToPlay.get(0).getValue())    // Checks if all the cards to play are the same
                        allSame = true;
                    else {
                        allSame = false;
                        break;
                    }
                }
                return cardsToPlay.get(0).getValue() >= lastPlayed.get(lastPlayed.size() - 1).getValue() && allSame;
            }
        }
        return false;
    }

    // Pass turn
    public void relinquishTurn() {
        cancel(); // Deselects any and all cards selected
        stateTracker.passTurn();
    }

    // TODO: Når spiller får beskjed fra server om ny runde, playCardsBtn.setText("Give Cards")
    // TODO: Kjør også giveUpCards() neste runde uavhengig
    // TODO: Neste runde så må cardsClickable også settes basert på rolle
    // Whenever the round starts, the server should run each player's giveUpCards()
    public void giveUpCards() {
        if (role != 0) {
            passTurnBtn.setEnabled(false);  // TODO: sett til true etter at kort er gitt
            int amountOfCards = Math.abs(role); // Get the amount of cards to be relinquished
            if (role < 0) {
                cancelBtn.setEnabled(false);
                // Highest cards to be selected
                for (int i = 0; i < amountOfCards; i++) {   // Loop from the highest valued cards
                    Card temp = hand.get(i);
                    if (temp.getValue() != 16) {            // If it is 3 of clubs, skip it
                        temp.setSelected();
                        cardsToPlay.add(hand.get(i));
                    } else
                        amountOfCards++;
                }
                playCardsBtn.setEnabled(true);
            } else {
                cardsClickable = true;  // If you are not bum/vice bum
                // Pick cards to give up (role is higher than neutral), if it is not right amount, disable button
                playCardsBtn.setEnabled(cardsToPlay.size() == amountOfCards);
            }
            playCardsBtn.setText("Give Cards");
            rearrangeCardsOnDisplay();
        } else
            cardsClickable = true;
    }
}
