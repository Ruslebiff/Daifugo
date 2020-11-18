package client;

import common.PlayerData;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;

import static client.GameLobby.LOGGER;

// Class shows the players in the current game, their respective roles and whose turn it is.
public class PlayersInformation extends JPanel {
    private GameStateTracker stateTracker;
    private List<PlayerData> players;     // Array of the players in the game
    private JLabel[] playerInfo;        // Array of the labels representing the players
    private int previousTurn;           // ID of the player before you
    private Color activeColor = new Color(135,206,250);
    private Color neutralColor = new Color(119,136,153);
    private HashMap<Integer, String> roleIdentifier;
    private final int WIDTH;
    private final int HEIGHT;
    private JLabel infoString;

    public PlayersInformation(GameStateTracker stateTracker) {
        this.stateTracker = stateTracker;
        this.players = stateTracker.getPlayerList();
        WIDTH = 200;
        HEIGHT = 50;
        int PANEL_HEIGHT = (players.size()+1) * HEIGHT;

        setLayout(new GridLayout(players.size()+1,0));
        setSize(new Dimension(WIDTH, PANEL_HEIGHT));

        infoString = new JLabel("Players", SwingConstants.CENTER);
        infoString.setFont(new Font("sans serif", Font.BOLD, 18));
        infoString.setBounds(0, 0, WIDTH, HEIGHT);
        add(infoString);
    }

    private void updatePanel() {
        setSize(new Dimension(WIDTH, (players.size()+1) * HEIGHT));
    }

    // Change color to the current player and turn the others gray
    public void indicateTurn() {
        updateTable();
        int playerIndex = stateTracker.getActivePlayerIndex();   // Get the ID of the active player

        if(previousTurn != playerIndex) {       // If the turn is supposed to go to the next player, else do nothing
            playerInfo[previousTurn].setBackground(neutralColor);
            playerInfo[playerIndex].setBackground(activeColor);
            previousTurn = playerIndex;
        }
        repaint();
    }

    public void updateTable() {
        players = stateTracker.getPlayerList();    // Update the list of players
        LOGGER.info("updating table, " + players.size() + " players in game");
        updatePanel();

        remove(infoString);
        if (playerInfo != null)
            for (JLabel pi : playerInfo)
                remove(pi);

        playerInfo = new JLabel[players.size()];  // For each player in the game, create a JLabel
        add(infoString);
        for (int i = 0; i < players.size(); i++) {
            String playerInformationTxt =
                    players.get(i).getNick() + " - " +
                    players.get(i).getRole() + " - " +
                    players.get(i).getNumberOfCards();
            playerInfo[i] = new JLabel(playerInformationTxt, SwingConstants.CENTER);
            playerInfo[i].setBounds(0, HEIGHT+(HEIGHT*i), WIDTH, HEIGHT);
            playerInfo[i].setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));  // Create border
            playerInfo[i].setBackground(neutralColor);
            playerInfo[i].setOpaque(true);
            add(playerInfo[i]);
        }
        repaint();
    }
}
