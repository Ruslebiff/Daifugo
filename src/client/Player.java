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

import static java.lang.Math.round;

public class Player extends JPanel implements GameStateTracker{
    private BufferedImage image;    // Image of green felt
    private final String filePath;  // Path to image of green felt
    private final String name;      // Name of player
    private final int playerID;     // Id
    private final int role;      // Role, -2 = Bum, -1 = ViceBum, 0 = Neutral, 1 = VP, 2 = President
    private final ArrayList<Card> hand; // The cards dealt to the player
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


    public Player(String name, int playerID, ArrayList<Card> cards, int width) {
        this.name = name;
        this.playerID = playerID;
        this.role = 2; // Neutral
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

        passTurnBtn = new PlayerButton((widthOfComponent/3)-(buttonWidth) + 15, 0, // Relinquishes turn
                                        buttonWidth, buttonHeight, "Pass Turn");
        passTurnBtn.addActionListener(e -> relinquishTurn());
        add(passTurnBtn);

        playCardsBtn = new PlayerButton((widthOfComponent/3)-(buttonWidth/2) + passTurnBtn.getBounds().x, 0,
                                        buttonWidth, buttonHeight, "Play Cards");
        playCardsBtn.setEnabled(false);


        cancelBtn = new PlayerButton((widthOfComponent/3)-(buttonWidth/2) + playCardsBtn.getBounds().x, 0,
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
        addCard.setBounds(0,175,buttonWidth,buttonHeight);
        add(addCard);
        addCard.addActionListener( e -> {
            viewDealtHand();
        });

        removeCard = new JButton("Remove");
        removeCard.setBounds(100,175,100,50);
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
                if(myTurn && cardsClickable) {
                    c.setSelected();    // Set the object to either selected or not, based upon what it previously was
                    c.paintComponent(c.getGraphics());  // Paint it accordingly
                    hand.forEach(c -> repaint());       // Repaint all the rest of the cards

                    if(c.isSelected())      // If the card is selected, add it to the arrayList
                        cardsToPlay.add(c);
                    else
                        cardsToPlay.remove(c);

                    if(!giveCards)
                        playCardsBtn.setEnabled(checkIfPlayable() && myTurn);
                    else // If the round has just started and you have to relinquish cards
                        giveUpCards();  // Function checks role and cards you have to give based on role
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
        hand.forEach(this::remove); // Remove all the cards on the GUI to repaint them centered
        // The spacing between cards
        space = space + ((maxCards - hand.size())/2);

        if(hand.size() < 4)  // If the cards on the hand is less than four, don't create any space
            space = cardWidth;

        // TODO: Y-coordinate must be further down
        // The x-coordinate of the first card from right to left
        boundsX = round(((float)widthOfComponent/2)  - ((float)cardWidth/2) + (((float)hand.size()-1)/2) * (float)space);
        int i = 0;
        for (Card card: hand) {     // For each card, space it more and more to the left
            card.setBounds(boundsX - ((i++)*space), 50, cardWidth, cardHeight);
            add(card);  // Add the cards again with the updated coordinates
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
        for(Card c: cardsToPlay)                    // Go through each card
            if (val != c.getValue()) {              // If the value is not the same, one of the cards is different
                equal = false;
                break;
            }
        return equal;
    }

    // Function removes cards from hand and GUI and sorts the remaining cards
    public void playCards() {
        cardsToPlay.forEach(c -> {
           hand.remove(c);     // Remove them from hand
           this.remove(c);     // Remove them from GUI
        });
        if(hand.size() != 0){   // If player has more cards left
           sortHand();             // Sort hand accordingly
           rearrangeCardsOnDisplay(); // Display properly
        } // else Server.endGameForPlayer, assign role accordingly
        cardsToPlay.removeAll(cardsToPlay); // Remove all cards to play from cards to play

        if(giveCards) {
            playCardsBtn.setText("Play Cards");
            giveCards = false;
            cardsClickable = true;
        }
        // TODO: return the cards played to the server or to another players hand
    }

    @Override
    public String getActivePlayerID() {
        return null;
    }

    @Override
    public ArrayList<Card> dealPlayerHand(String token) {
        return null;
    }

    @Override
    public Boolean checkIfPlayable() {
        int nCards = cardsToPlay.size();  // Amount of cards played
        int [] lastThreeCards = new int[3];
        lastThreeCards[0] = 8;
        lastThreeCards[1] = 9;
        lastThreeCards[2] = 4;

        // Check if player has selected cards to play, and if they match the types on the table, i.e. singles, doubles
        // or triples, alternatively that the card played is clover 3
        if(nCards != 0 && ((nCards == getLastPlayedType()) || (cardsToPlay.get(nCards - 1).getValue() == 16))) {
            // Check if the card on top of the deck is higher or equal to the player's card
            boolean valid = true;
            for (Card c: cardsToPlay)     // Check that all selected cards are playable
                if (c.getValue() < lastThreeCards[2]) { // TODO: CHANGE lastThreeCards to the servers cards
                    valid = false;  // If a card is not playable, set valid to false
                    break;
                }
            if(!allTheSameCards())  // If the selected cards vary in value, validity is false
                valid = false;

            return valid;
        }
        return false;
    }

    @Override
    public int getLastPlayedType() {
        // 1 = single, 2 = double, 3 = triple
        return 3;
    }

    @Override
    public void relinquishTurn() {
        // Next turn
        cancel(); // Deselects any and all cards selected
        myTurn = false; // TODO: Whenever it is my turn again, set myTurn = TRUE
        toggleAllButtons();
    }

    @Override
    public void playCards(ArrayList<Card> playedCards) {

    }


    // TODO: Når spiller får beskjed fra server om ny runde, playCardsBtn.setText("Give Cards")
    // TODO: Kjør også giveUpCards() neste runde uavhengig
    // TODO: Neste runde så må cardsClickable også settes basert på rolle
    // Whenever the round starts, the server should run each player's giveUpCards()
    public void giveUpCards() {
        if(role != 0) {
            passTurnBtn.setEnabled(false);  // TODO: Når server indikerer at det er "min" tur, pass knapp og cancel knapp true
            int amountOfCards = Math.abs(role); // Get the amount of cards to be relinquished
            if(role < 0) {
                cancelBtn.setEnabled(false);
                // Highest cards to be selected
                for (int i = 0; i < amountOfCards; i++) {
                    Card temp = hand.get(i);
                    if(temp.getValue() != 16) {
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
     *  spillerID/uid i en List, sjekker størrelsen på den listen, hvis den
     *  lista er antallSpillere-1 så er spillet over, og roller deles ut
     * 	- Må ta rede for at det kan være 3-8 spillere
     * 	- Dersom det er 3 spillere, bare sett rollene på dem manuelt
     * 	- Hvis det er flere:
     * 		* fori-loop
     * 			if(i == 0): player.assignRole(2)
     * 			elif(i == 1): player.assignRole(1)
     * 			elif(i == (antall.size() - 2): player.assignRole(-1)
     * 			elif(i == (antall.size() - 1): player.assignRole(-2)
     * 	- Etter at roller deles ut, start et nytt spill
     * 	- Del ut kort
     *
     */

    // Enables/disables all the buttons based on what they previously were
    public void toggleAllButtons() {
        playCardsBtn.setEnabled(!playCardsBtn.isEnabled());
        passTurnBtn.setEnabled(!passTurnBtn.isEnabled());
        cancelBtn.setEnabled(!cancelBtn.isEnabled());
    }
}
