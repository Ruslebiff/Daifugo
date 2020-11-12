package client;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

// Class shows the players in the current game, their respective roles and whose turn it is.
public class PlayersInformation extends JPanel implements GameStateTracker {
    private final Player[] players;     // Array of the players in the game
    private JLabel[] playerInfo;        // Array of the labels representing the players
    private int previousTurn;           // ID of the player before you
    private Color turnColor = new Color(135,206,250);
    private Color neutralColor = new Color(119,136,153);
    private HashMap<Integer, String> roleIdentifier;

    public PlayersInformation(Player[] players) {
        this.players = players;
        int WIDTH = 100;
        int HEIGHT = 50;
        int PANEL_HEIGHT = players.length * HEIGHT;
        playerInfo = new JLabel[players.length];

        roleIdentifier = new HashMap<Integer, String>();
        roleIdentifier.put(-2, "Bum");
        roleIdentifier.put(-1, "Vice Bum");
        roleIdentifier.put(0, "Neutral");
        roleIdentifier.put(1, "Vice President");
        roleIdentifier.put(2, "President");

        setLayout(new GridLayout(0,1));
        setSize(new Dimension(WIDTH, PANEL_HEIGHT));

        JLabel infoString = new JLabel("Players", SwingConstants.CENTER);
        infoString.setFont(new Font("sans serif", Font.BOLD, 20));
        add(infoString);

        // For each player in the game, create a JLabel
        for (int i = 0; i < players.length; i++) {
            String playerInformationTxt = players[i].getName() + " - " + roleIdentifier.get(players[i].getRole());
            playerInfo[i] = new JLabel(playerInformationTxt, SwingConstants.CENTER);
            playerInfo[i].setSize(new Dimension(WIDTH, HEIGHT));
            playerInfo[i].setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));  // Create border
            playerInfo[i].setBackground(neutralColor);
            playerInfo[i].setOpaque(true);
            add(playerInfo[i]);
        }
        indicateTurn();
    }

    // Change color to the current player and turn the others gray
    public void indicateTurn() {
        int pID = Integer.parseInt(getActivePlayerID());    // Get the ID of the active player

        if(previousTurn != pID) {       // If the turn is supposed to go to the next player, else do nothing
            playerInfo[previousTurn].setBackground(neutralColor);
            playerInfo[pID].setBackground(turnColor);
            previousTurn = pID;
        }
    }

    @Override
    public String getActivePlayerID() {
        return "0";
    }

    @Override
    public ArrayList<Card> dealPlayerHand(String token) {
        return null;
    }

    @Override
    public Boolean checkIfPlayable() {
        return null;
    }

    @Override
    public int getLastPlayedType() {
        return 0;
    }

    @Override
    public void relinquishTurn() {

    }
}
