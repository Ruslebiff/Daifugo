package client;

import common.PlayerData;

import javax.swing.*;
import java.awt.*;
import java.util.List;


import static client.GameLobby.LOGGER;

// Class shows the players in the current game, their respective roles and whose turn it is.
public class PlayersInformation extends JPanel {
    private final GameStateTracker stateTracker;
    private List<PlayerData> players;     // Array of the players in the game
    private JLabel[] playerInfo;        // Array of the labels representing the players
    private final Color activeColor = new Color(135,206,250);
    private final Color neutralColor = new Color(119,136,153);
    private final Color passColor = new Color(250,128,114);
    private final int WIDTH;
    private final int HEIGHT;
    private final JLabel infoString;

    public PlayersInformation(GameStateTracker stateTracker) {
        this.stateTracker = stateTracker;
        this.players = stateTracker.getPlayerList();
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

        if (playerIndex >= 0)
            playerInfo[playerIndex].setBackground(activeColor);
        repaint();
    }

    public void updateTable() {
        players = stateTracker.getPlayerList();    // Update the list of players
        LOGGER.fine("updating table, " + players.size() + " players in game");

        updatePanel();

        remove(infoString);
        if (playerInfo != null)
            for (JLabel pi : playerInfo)
                remove(pi);

        playerInfo = new JLabel[players.size()];  // For each player in the game, create a JLabel
        add(infoString);
        for (int i = 0; i < players.size(); i++) {
            PlayerData player = this.players.get(i);
            String role;

            switch(player.getRole()) {
                case PRESIDENT -> role ="P";
                case VICE_PRESIDENT -> role = "VP";
                case NEUTRAL -> role = "N";
                case VICE_BUM -> role = "VB";
                case BUM -> role = "B";
                default -> role = "";
            }


            String playerInformationTxt = player.getNick() + " - " + role;


            playerInfo[i] = new JLabel(playerInformationTxt, SwingConstants.CENTER);
            playerInfo[i].setBounds(0, HEIGHT+(HEIGHT*i), WIDTH, HEIGHT);
            playerInfo[i].setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));  // Create border

            int noOfCards = player.getNumberOfCards();

            playerInfo[i].setBackground(neutralColor);

            if(noOfCards != 0 && stateTracker.isStarted()) {
                playerInformationTxt += " - " + noOfCards;
            }
            if (player.hasPassed()) {
                playerInformationTxt += " - PASS";
                playerInfo[i].setBackground(passColor);
            } else if (stateTracker.isStarted() && noOfCards == 0){
                playerInformationTxt += " - DONE";
                playerInfo[i].setBackground(new Color(152,251,152));
            }

            playerInfo[i].setText(playerInformationTxt);

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
