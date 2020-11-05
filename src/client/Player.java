package client;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Player extends JPanel {
    private final String name;
    private final int playerID;
    private final String role;
    private final ArrayList<Card> hand;
    private JButton removeCard;
    private JButton addCard;

    public Player(String name, int playerID, String role, ArrayList<Card> cards) {
        this.name = name;
        this.playerID = playerID;
        this.role = role;
        this.hand = cards;

        addCard = new JButton("Add");
        removeCard = new JButton("Remove");

        setOpaque(true);
        setBackground(new Color(79,71,68));
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));  // Create border
        setLayout(new GridBagLayout()); // The row where the cards will show
        GridBagConstraints gbc = new GridBagConstraints();  // Constraints of the grid
        gbc.gridwidth = 1;   // Set the width of the cell of the cards
        gbc.fill = GridBagConstraints.BOTH;  // Make the component fill out the cell
        gbc.gridx = 0;  // Where the first component will be placed
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;

        sortHand();
        for (int i = 0; i < hand.size(); i++) {
            if(i == (hand.size() - 1)) {
                gbc.weightx = 5;
            }
            this.add(hand.get(i), gbc);     // Add each card to the hand
            gbc.gridx += i;                 // Set where the card should be set
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
}
