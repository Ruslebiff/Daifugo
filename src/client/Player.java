package client;

import client.networking.ClientConnection;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Math.round;

public class Player extends JPanel{
    private GameStateTracker stateTracker;
    private BufferedImage image;    // Image of green felt
    private final String filePath;  // Path to image of green felt
    private final String name;      // Name of player
    private final int playerID;     // Id
    private int role;      // Role, -2 = Bum, -1 = ViceBum, 0 = Neutral, 1 = VP, 2 = President
    private ArrayList<Card> hand; // The cards dealt to the player
    private final ArrayList<Card> cardsToPlay = new ArrayList<>();
    private JButton removeCard;
    private JButton addCard;
    private final PlayerButton playCardsBtn;   // Button plays the selected cards
    private final PlayerButton cancelBtn;
    private final PlayerButton passTurnBtn;
    private final int cardWidth = 80;
    private final int cardHeight = 120;
    private final int widthOfComponent;
    private final int buttonWidth = 100;
    private final int buttonHeight = 50;
    private int boundsX = 0;
    private int space = 24; // Space between cards when a player has maximum cards
    private final int maxCards = 18;
    private boolean myTurn = true;
    private boolean giveCards = false; // Is set by server
    private boolean cardsClickable = true;
    private ClientConnection conn = null;


    public Player(String name, int playerID, ArrayList<Card> cards, int width, GameStateTracker sT) {
        this.stateTracker = sT;
        this.name = name;
        this.playerID = playerID;
        this.role = 2; // Upon creation of a player, the player will be set to neutral, as the game has just begun
        this.hand = cards;
        this.widthOfComponent = width;
//        if(role != 0)       // TODO: Server should set this value on new round
//            giveCards = true;
        sortHand(); // Sorts the players hand with respect to the card values
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

        // TODO: REMOVE ADD AND REMOVE BUTTONS
        addCard = new JButton("Add");
        addCard.setBounds(0, 175, buttonWidth, buttonHeight);
        add(addCard);
        addCard.addActionListener(e -> {
            viewDealtHand();
        });

        removeCard = new JButton("Remove");
        removeCard.setBounds(100, 175, 100, 50);
        add(removeCard);
        removeCard.addActionListener(e -> {
            removeCardFromDisplay();
        });

//        giveUpCards();  // TODO: FJERN
    }

    public void addListener(Card c) {

        c.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {    // Upon selection, paint/unpaint the component with overlay
                if (stateTracker.getIsMyTurn() && cardsClickable) {
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

    public String getName() {
        return name;
    }

    public int getRole() {
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

    // Function checks if all the cards selected to play are the same value
    public Boolean allTheSameCards() {
        boolean equal = true;
        int val = cardsToPlay.get(0).getValue();    // Get the value from one of the cards
        for (Card c : cardsToPlay)                    // Go through each card
            if (val != c.getValue()) {              // If the value is not the same, one of the cards is different
                equal = false;
                break;
            }
        return equal;
    }

    // Function removes cards from hand and GUI and sorts the remaining cards
    public void playCards() {
        if(!giveCards) {   // If it is a normal round, play the cards
            ArrayList<Card> playedInRound = stateTracker.getRoundCards();
            ArrayList<Card> lastCards = new ArrayList<>();
            if(cardsToPlay.size() < 3 && playedInRound.size() >= 2) {
                // If the cards played potentially removes the played cards total by the four equal cards rule
                for (int i = playedInRound.size() - 1; i > playedInRound.size() - 4 + cardsToPlay.size() - 1; i--) {
                    lastCards.add(playedInRound.get(i));
                }
                // Check if the last cards plus cards to play are all equal cards and are four total
                int counter = 0;
                if(cardsToPlay.size() == 1) {
                    counter = 1;
                    for (Card c : lastCards) {  // If the value is the same, increment the counter
                        if(c.getValue() == cardsToPlay.get(0).getValue())
                            counter++;
                    }
                } else if(cardsToPlay.size() == 2) {
                    counter = 2;
                    for (Card c : lastCards) {
                        if(c.getValue() == cardsToPlay.get(0).getValue())
                            counter++;
                    }
                }
                if(counter == 4) {
                    // TODO: Implement action that resets the cards
                }
            }

            if(stateTracker.playCards(cardsToPlay)) {
                stateTracker.setNextTurn();
            }

        } else {            // If it is at the beginning of the round
            playCardsBtn.setText("Play Cards");
            giveCards = false;
            cardsClickable = true;
            if(stateTracker.giveCards(cardsToPlay, role))
                stateTracker.setNextTurn();
        }

        // Clean up the buffer of cards to play
        cardsToPlay.forEach(c -> {
            hand.remove(c);     // Remove them from hand
            this.remove(c);     // Remove them from GUI
        });
        if (hand.size() != 0) {   // If player has more cards left
            sortHand();             // Sort hand accordingly
            rearrangeCardsOnDisplay(); // Display properly
        } // else Server.endGameForPlayer, assign role accordingly
        cardsToPlay.removeAll(cardsToPlay); // Remove all cards to play from cards to play
    }

    public Boolean checkIfPlayable() {
        if(cardsToPlay.size() != 0) {   // If the player has selected cards
            ArrayList<Card> lastPlayed = stateTracker.getLastPlayedCards(); // Last played cards

            if (cardsToPlay.size() == 1 && cardsToPlay.get(0).getValue() == 16) // If player selected 3 of clubs
                return true;

            // Check that the amount of selected cards is the same as the last played cards
            if (cardsToPlay.size() == lastPlayed.size()) {
                boolean allSame = false;
                for (Card card : cardsToPlay) {
                    if (card == cardsToPlay.get(0))    // Checks if all the cards to play are the same
                        allSame = true;
                    else {
                        allSame = false;
                        break;
                    }
                }
                return cardsToPlay.get(0).getValue() >= lastPlayed.get(0).getValue() && allSame;
            }
        }
        return false;
    }


    public void relinquishTurn() {
        // Next turn
        cancel(); // Deselects any and all cards selected
        myTurn = false; // TODO: Whenever it is my turn again, set myTurn = TRUE
    }

    // TODO: Når spiller får beskjed fra server om ny runde, playCardsBtn.setText("Give Cards")
    // TODO: Kjør også giveUpCards() neste runde uavhengig
    // TODO: Neste runde så må cardsClickable også settes basert på rolle
    // Whenever the round starts, the server should run each player's giveUpCards()
    public void giveUpCards() {
        if (role != 0) {
            passTurnBtn.setEnabled(false);  // TODO: Når server indikerer at det er "min" tur, pass knapp og cancel knapp true
            int amountOfCards = Math.abs(role); // Get the amount of cards to be relinquished
            if (role < 0) {
                cancelBtn.setEnabled(false);
                // Highest cards to be selected
                for (int i = 0; i < amountOfCards; i++) {
                    Card temp = hand.get(i);
                    if (temp.getValue() != 16) {
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

    /**
     * TODO: Server
     * *Når en spiller er tom for kort, si i fra til server -> server legger til
     * spillerID/uid i en List, sjekker størrelsen på den listen, hvis den
     * lista er antallSpillere-1 så er spillet over, og roller deles ut
     * - Må ta rede for at det kan være 3-8 spillere
     * - Dersom det er 3 spillere, bare sett rollene på dem manuelt
     * - Hvis det er flere:
     * * fori-loop
     * if(i == 0): player.assignRole(2)
     * elif(i == 1): player.assignRole(1)
     * elif(i == (antall.size() - 2): player.assignRole(-1)
     * elif(i == (antall.size() - 1): player.assignRole(-2)
     * - Etter at roller deles ut, start et nytt spill
     * - Del ut kort
     */

    public void setHand(ArrayList<Card> dealtCards) {
        this.hand = dealtCards;
    }

    public void setRole(int role) {
        this.role = role;
    }
}
