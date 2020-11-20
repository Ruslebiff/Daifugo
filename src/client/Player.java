package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static client.GameLobby.LOGGER;
import static java.lang.Math.round;

public class Player extends JPanel{
    private final GameStateTracker stateTracker;
    private BufferedImage image;    // Image of green felt
    private int role;      // Role, -2 = Bum, -1 = ViceBum, 0 = Neutral, 1 = VP, 2 = President
    private List<Card> hand ; // The cards dealt to the player
    private List<Card> cardsToPlay = new ArrayList<>();
    private final PlayerButton playCardsBtn;   // Button plays the selected cards
    private final PlayerButton cancelBtn;
    private final PlayerButton passTurnBtn;
    private final int cardWidth = 80;
    private final int cardHeight = 120;
    private final int widthOfComponent;
    private int boundsX = 0;
    private int space = 24; // Space between cards when a player has maximum cards
    private int maxSpace = 24;
    private final int maxCards = 18;
    private boolean giveCards = false; // Is set by server
    private boolean cardsClickable;


    public Player(int width, GameStateTracker sT) {
        this.stateTracker = sT;
        this.role = 0; // Upon creation of a player, the player will be set to neutral, as the game has just begun
        widthOfComponent = width;
        setLayout(null);
        setOpaque(true);

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
                buttonWidth, buttonHeight, "Pass");
        passTurnBtn.addActionListener(e -> relinquishTurn());
        add(passTurnBtn);

        playCardsBtn = new PlayerButton((widthOfComponent / 3) - (buttonWidth / 2) + passTurnBtn.getBounds().x, 0,
                buttonWidth, buttonHeight, "Play");
        playCardsBtn.setEnabled(false);


        cancelBtn = new PlayerButton((widthOfComponent / 3) - (buttonWidth / 2) + playCardsBtn.getBounds().x, 0,
                buttonWidth, buttonHeight, "Cancel");
        cancelBtn.addActionListener(e -> cancel());
        cancelBtn.setEnabled(false);
        add(cancelBtn);

        playCardsBtn.addActionListener(e -> {
            playCards();
        });
        add(playCardsBtn);
    }

    public void update(List<Card> newHand) {
        if (hand != null) {
            for (Card card : hand) {
                this.remove(card);
            }
            repaint();
        }
        hand = newHand;
        sortHand(); // Sorts the players hand with respect to the card values
        hand.forEach(this::addListener);    // Adds a mouseListener for each card
        viewDealtHand();
        passTurnBtn.setEnabled(stateTracker.isMyTurn() && !stateTracker.getCardsOnTable().isEmpty());
    }

    public void setTradingPhase(boolean phase) {
        this.giveCards = phase;
        if (phase)
            cardsClickable = true;
    }

    public void updateButtonState() {
        if (!stateTracker.isTradingPhase()) {       // If it is my turn and there are cards on table, enable button
            passTurnBtn.setEnabled(stateTracker.isMyTurn() && !stateTracker.getCardsOnTable().isEmpty());
            cardsClickable = stateTracker.isMyTurn();
            tradeMode(false);
        } else {
            tradeMode(true);
            passTurnBtn.setEnabled(false);
        }
    }

    public void addListener(Card c) {

        c.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {    // Upon selection, paint/unpaint the component with overlay

                if (stateTracker.isTradingPhase() && stateTracker.iHaveToTrade()) {
                    c.setSelected();    // Set the object to either selected or not, based upon what it previously was
                    c.paintComponent(c.getGraphics());  // Paint it accordingly
                    hand.forEach(c -> repaint());       // Repaint all the rest of the cards

                    if (c.isSelected()) {      // If the card is selected, add it to the arrayList
                        cardsToPlay.add(c);
                        cancelBtn.setEnabled(true);
                    }
                    else
                        cardsToPlay.remove(c);

                    playCardsBtn.setEnabled(checkIfGivable());
                } else {
                    LOGGER.info("clicked, my turn: " + cardsClickable);
                    if (stateTracker.isMyTurn() && cardsClickable) {
                        c.setSelected();    // Set the object to either selected or not, based upon what it previously was
                        c.paintComponent(c.getGraphics());  // Paint it accordingly
                        hand.forEach(c -> repaint());       // Repaint all the rest of the cards

                        if (c.isSelected())      // If the card is selected, add it to the arrayList
                            cardsToPlay.add(c);
                        else
                            cardsToPlay.remove(c);

                        playCardsBtn.setEnabled(checkIfPlayable());
                        cancelBtn.setEnabled(true);
                    }
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

    public void viewDealtHand() {
        space = maxSpace;
        space = space + ((maxCards - hand.size()) / 2);
        boundsX = (widthOfComponent / 2) - (cardWidth / 2) + (((hand.size() - 1) / 2) * space);
        if(hand.size() == 18)
            boundsX += 10;
        int i = 0;
        for (Card card : hand) {
            card.setBounds(boundsX - ((i++) * space), 60, cardWidth, cardHeight);
            add(card);
        }
        repaint();
    }

    public void rearrangeCardsOnDisplay() {
        hand.forEach(this::remove); // Remove all the cards on the GUI to repaint them centered
        space = space + ((maxCards - hand.size()) / 2); // The spacing between cards

        if (hand.size() < 4)  // If the cards on the hand is less than four, don't create any space
            space = cardWidth;

        // The x-coordinate of the first card from right to left
        boundsX = round(((float) widthOfComponent / 2) - ((float) cardWidth / 2) + (((float) hand.size() - 1) / 2) * (float) space);
        int i = 0;
        for (Card card : hand) {     // For each card, space it more and more to the left
            card.setBounds(boundsX - ((i++) * space), 60, cardWidth, cardHeight);
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
        cardsToPlay = new ArrayList<>();
        cancelBtn.setEnabled(false);
    }

    // Function removes cards from hand and GUI and sorts the remaining cards
    public void playCards() {
        if(!stateTracker.isTradingPhase()) {   // If it is a normal round, play the cards
            LOGGER.info("Attempting to play cards...");
            boolean ok;
            if (cardsToPlay.isEmpty()) {
                LOGGER.warning("cards to play was empty");
                return;
            }


            ok = stateTracker.playCards(cardsToPlay);
            if (!ok) {
                LOGGER.warning("Unable to play the selected cards.");
                cancel();
                return;
            }
            playCardsBtn.setEnabled(stateTracker.isMyTurn());

        } else {            // If it is at the beginning of the round
            LOGGER.info("Trading cards with another player...");
            if(!stateTracker.giveCards(cardsToPlay)) {   // Give cards to player
                LOGGER.warning("Unable to give selected cards, server error");
                return;
            } else {
                LOGGER.info("Trade went well... ?");
            }
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

        cardsToPlay = new ArrayList<>(); // Remove all cards to play from cards to play
        playCardsBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        cardsClickable = false;
        giveCards = false;
        repaint();
    }

    public Boolean checkIfPlayable() {
        if(cardsToPlay.size() != 0) {   // If the player has selected cards
            List<Card> allCardsOnTable = stateTracker.getCardsOnTable(); // Last played cards
            List<Card> lastPlayed = new ArrayList<>();

            int noOfCardsInLastTrick = stateTracker.getCardsInTrick();
            int allCards = allCardsOnTable.size() - 1;
            for (int i = allCards; i > allCards - noOfCardsInLastTrick; i--) {  // Get only the amount of cards from
                lastPlayed.add(allCardsOnTable.get(i));                         // table equal to last trick
            }

            if (lastPlayed.size() == 0)
                return true;

            // Check that you don't end up with 3 of clubs
            if(
                    hand.size() == 2 &&     // If you have two cards left
                    cardsToPlay.get(0).getValue() != 16 && // and you want to play a card that is not 3 of clubs
                    (hand.get(0).getValue() == 16 || hand.get(1).getValue() == 16) // The remaining card is 3 of clubs
            ){
                LOGGER.warning("Can't have three of clubs as last card");
                JOptionPane.showMessageDialog(
                        this.getParent(),
                        "Can't have three of clubs as last card!",
                        "Illegal Play",
                        JOptionPane.WARNING_MESSAGE
                );
                cancel();
                return false;
            }

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

    public boolean checkIfGivable() {
        if(cardsToPlay.size() == 0)
            return false;

        int noOfCardsToGive = Math.abs(stateTracker.getMyRoleNumber());
        if (cardsToPlay.size() != noOfCardsToGive )
            return false;

        boolean mustChooseHighest = stateTracker.getMyRoleNumber() < 0;

        for (Card card : cardsToPlay) {
            if (card.getValue() == 16)  // 3 of clubs cannot be given
                return false;

            if (mustChooseHighest) {
                for (Card handCard : hand) {
                    if (handCard.getValue() > card.getValue() && handCard.getValue() != 16 )
                        return false;
                }
            }
        }

        return true;
    }

    // Pass turn
    public void relinquishTurn() {
        cancel(); // Deselects any and all cards selected
        playCardsBtn.setEnabled(false);
        stateTracker.passTurn();
    }

    public void tradeMode(boolean on) {
       if (on) {
           playCardsBtn.setText("Give");
       } else {
           playCardsBtn.setText("Play");
       }
    }

}
