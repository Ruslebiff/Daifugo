package client;

import common.PlayerData;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

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
        previousTurn = 0;
        WIDTH = 200;
        HEIGHT = 50;
        int PANEL_HEIGHT = (players.size()+1) * HEIGHT;

        setLayout(null);
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

        LOGGER.info("Changing player turn indicator");
        if (playerIndex >= 0)
            playerInfo[playerIndex].setBackground(activeColor);
        LOGGER.info("Player has no of cards " + players.get(0).getNumberOfCards());
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
            String playerInformationTxt = players.get(i).getNick() + " - " + players.get(i).getRole();
            int noOfCards = players.get(i).getNumberOfCards();
            if(noOfCards != 0)
                playerInformationTxt += " - " + noOfCards;

            LOGGER.info("Player information: " + playerInformationTxt );
            if(stateTracker.getPlayerList().get(0).getNumberOfCards() != 0)
                LOGGER.info("HALLO " + (stateTracker.getPlayerList().get(0).getNumberOfCards()));
            else
                LOGGER.info(players.get(i).getNick() + " has no cards");

            playerInfo[i] = new JLabel(playerInformationTxt, SwingConstants.CENTER);
            playerInfo[i].setBounds(0, HEIGHT+(HEIGHT*i), WIDTH, HEIGHT);
            playerInfo[i].setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));  // Create border
            playerInfo[i].setBackground(neutralColor);
            playerInfo[i].setOpaque(true);
            if(i != 0) {
                playerInfo[i].setBounds(0, playerInfo[i - 1].getY() + HEIGHT, WIDTH, HEIGHT);
            } else {
                playerInfo[i].setBounds(0, infoString.getY() + HEIGHT, WIDTH, HEIGHT);
            }
            add(playerInfo[i]);
        }
    }
}
