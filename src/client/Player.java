package client;

import javax.swing.*;
import java.util.ArrayList;

public class Player extends JPanel {
    private final String name;
    private final int playerID;
    private String role;
    private  ArrayList<Card> hand;

    public Player(String name, int playerID, String role, ArrayList<Card> cards) {
        this.name = name;
        this.playerID = playerID;
        this.role = role;
        this.hand = cards;
        sortHand();
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
