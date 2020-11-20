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
    private boolean cardsClickable;
    private boolean goneOut = false;

    public void setGoneOut(boolean goneOut) {
        this.goneOut = goneOut;
    }

    public boolean isGoneOut() {
        return goneOut;
    }

    public Player(int width, GameStateTracker sT) {
        this.stateTracker = sT;
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
        } else {
            goneOut = true;
        }

        cardsToPlay = new ArrayList<>(); // Remove all cards to play from cards to play
        playCardsBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        cardsClickable = false;
        repaint();
    }

    public Boolean checkIfPlayable() {
        if(cardsToPlay.size() != 0) {   // If the player has selected cards
            List<Card> lastPlayed = stateTracker.getCardsOnTable();

            boolean c3 = false;

            // check that all cards tried to be played are the same
            if (cardsToPlay.size() > 1) {
                for (Card c : cardsToPlay) {
                    if (c.getValue() == 16)
                        c3 = true;
                    if (c.getValue() != cardsToPlay.get(0).getValue()) {
                        return false;
                    }
                }
            }



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


            if (!lastPlayed.isEmpty() && cardsToPlay.size() == 1 && cardsToPlay.get(0).getValue() == 16) // If player selected 3 of clubs
                return true;

            // if the table is empty, all cards are the same, and 3 of clubs isn't one of them
            if (lastPlayed.isEmpty() && !c3)
                return true;

            // Check that the amount of selected cards is the same as the last played cards
            if (cardsToPlay.size() == stateTracker.getCardsInTrick()) {
                return cardsToPlay.get(0).getValue() >= lastPlayed.get(lastPlayed.size() - 1).getValue();
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
                    if (noOfCardsToGive == 1){  // must only give one card
                        if (handCard.getValue() > card.getValue() && handCard.getValue() != 16 )
                            return false;
                    } else if (noOfCardsToGive == 2){
                        boolean handCardNotAmongSelected = true;

                        for (Card c : cardsToPlay) {
                            if (c.getNumber() == handCard.getNumber() && c.getSuit() == handCard.getSuit()){
                                handCardNotAmongSelected = false;
                            }
                        }

                        if (handCard.getValue() > card.getValue() && handCard.getValue() != 16 && handCardNotAmongSelected)
                            return false;
                    }

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
