package client;

import common.PlayerData;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

// Class shows the players in the current game, their respective roles and whose turn it is.
public class PlayersInformation extends JPanel {
    private GameStateTracker stateTracker;
    private final List<PlayerData> players;     // Array of the players in the game
    private JLabel[] playerInfo;        // Array of the labels representing the players
    private int previousTurn;           // ID of the player before you
    private Color turnColor = new Color(135,206,250);
    private Color neutralColor = new Color(119,136,153);
    private HashMap<Integer, String> roleIdentifier;

    public PlayersInformation(GameStateTracker stateTracker) {
        this.stateTracker = stateTracker;
        this.players = stateTracker.getPlayerList();
        int WIDTH = 100;
        int HEIGHT = 50;
        int PANEL_HEIGHT = players.size() * HEIGHT;

        setLayout(new GridLayout(0,1));
        setSize(new Dimension(WIDTH, PANEL_HEIGHT));


        JLabel infoString = new JLabel("Players", SwingConstants.CENTER);
        infoString.setFont(new Font("sans serif", Font.BOLD, 20));
        add(infoString);

        updateTable();
    }

    // Change color to the current player and turn the others gray
    public void indicateTurn() {
        int pID = Integer.parseInt(stateTracker.getActivePlayerID());    // Get the ID of the active player

        if(previousTurn != pID) {       // If the turn is supposed to go to the next player, else do nothing
            playerInfo[previousTurn].setBackground(neutralColor);
            playerInfo[pID].setBackground(turnColor);
            previousTurn = pID;
        }
    }
    
    // TODO: størrelse må være relativ til antall spillere med
    public void updateTable() {
        playerInfo = new JLabel[players.size()];  // For each player in the game, create a JLabel
        for (int i = 0; i < players.size(); i++) {
            String playerInformationTxt = players.get(i).getNick() + " - " + players.get(i).getRole();
            playerInfo[i] = new JLabel(playerInformationTxt, SwingConstants.CENTER);
            playerInfo[i].setSize(new Dimension(WIDTH, HEIGHT));
            playerInfo[i].setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));  // Create border
            playerInfo[i].setBackground(neutralColor);
            playerInfo[i].setOpaque(true);
            add(playerInfo[i]);
        }
    }

}
