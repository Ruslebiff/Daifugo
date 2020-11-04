package client;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Player extends JPanel {
    private final String name;
    private final int playerID;
    private String role;
    private  ArrayList<Card> hand;

    public Player(String name, int playerID, String role, ArrayList<Card> cards) {
        int WIDTH = 200;
        int HEIGHT = 200;
        this.name = name;
        this.playerID = playerID;
        this.role = role;
        this.hand = cards;
        sortHand();
        setLayout(new GridLayout(1,0)); // The row where the cards will show
        setOpaque(true);
        setBackground(new Color(79,71,68));
        setSize(new Dimension(cards.size() * WIDTH, HEIGHT));

        hand.forEach(this::add);    // Add the cards from the deck to your hand in the GUI
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
}
