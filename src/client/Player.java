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
        this.name = name;
        this.playerID = playerID;
        this.role = role;
        this.hand = cards;

        setLayout(new GridBagLayout()); // The row where the cards will show
        setOpaque(true);
        setBackground(new Color(79,71,68));

        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));  // Create border

        sortHand();
        for (int i = 0; i < hand.size(); i++) {
            hand.get(i).setPreferredSize(new Dimension());
//            https://docs.oracle.com/javase/tutorial/uiswing/layout/gridbag.html
        }
        // For løkke, legger til comp i, i.setPrefSize, if (i siste),
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
