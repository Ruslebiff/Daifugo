package client;

import client.networking.ClientConnection;
import common.GameListing;
import protocol.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class GameLobby extends JFrame {
    private String[] columnNames = {
            "ID",
            "Game name",
            "Owner",
            "Players",
            "Private",
            ""
    };
    private DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 5; // only last column, needed for button to work
        }
    };
    private JTable gamesTable = new JTable(tableModel);
    private int window_height = 1000;
    private int window_width = 1000;
    private int MAX_PLAYERS = 8;
    private String playerName;
    private ArrayList<Object> gameList = new ArrayList<>();
    private ClientConnection conn = null;

    public GameLobby() {
        try {
            conn = new ClientConnection("localhost");
        } catch (IOException e) {
            System.out.println("ERROR: Failed to connect!");
            e.printStackTrace();
        }

        try {
            Message response = conn.sendMessage(
                    new Message(MessageType.CONNECT)
            );
            IdentityResponse identityResponse = (IdentityResponse) response;
            if (response.isError()){
                System.out.println(response.getErrorMessage());
                return;
            }
            playerName = identityResponse.getNick();
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }


        /* Create window */
        setSize(window_width,window_height);
        setLayout(new BorderLayout());
        setTitle("Daifugo - Lobby");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // TODO: does disconnect message work?
                try {
                    Message response = conn.sendMessage(new Message(MessageType.DISCONNECT));
                    if (response.isError()){
                        System.out.println(response.getErrorMessage());
                    }

                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
            }
        });

        /* New game view */
        JPanel newGamePanel = new JPanel();
        newGamePanel.setSize(window_width,window_height);
        newGamePanel.setVisible(false);
        newGamePanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel newGameNameLabel = new JLabel("Name: ");
        JTextField newGameName = new JTextField("my new game");
        JPasswordField newGamePassword = new JPasswordField("");
        newGamePassword.setEnabled(false);
        JCheckBox newGamePrivateCheckbox = new JCheckBox("Private ");
        JButton newGameConfirmButton = new JButton("Confirm");

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        newGamePanel.add(newGameNameLabel, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        newGamePanel.add(newGameName, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        newGamePanel.add(newGamePrivateCheckbox, gbc);
        gbc.gridx = 1;
        newGamePanel.add(newGamePassword, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 3;
        gbc.gridx = 0;
        gbc.gridy = 2;
        newGamePanel.add(newGameConfirmButton, gbc);


        /* Settings view */
        JPanel settingsPanel = new JPanel();
        settingsPanel.setSize(window_width,window_height);
        settingsPanel.setVisible(false);
        settingsPanel.setLayout(new GridBagLayout());


        // TODO: newNickNameLabel is placed on top of newNickName text field
        JLabel newNickNameLabel = new JLabel("Nickname: ");
        JTextField newNickName = new JTextField(playerName);
        JButton settingsConfirmButton = new JButton("Confirm");

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        settingsPanel.add(newNickNameLabel, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        settingsPanel.add(newNickName, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 1;
        settingsPanel.add(settingsConfirmButton, gbc);

        /** Normal view: */
        /* Control bar */
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        controlPanel.setBackground(Color.lightGray);

        JButton newGameButton = new JButton();
        newGameButton.setText("New Game");


        JButton refreshGamesButton = new JButton();
        refreshGamesButton.setText("Refresh");
        refreshGamesButton.addActionListener(e -> {
            refreshGamesList();
        });

        JLabel nickText = new JLabel();
        nickText.setText(playerName);

        JButton settingsButton = new JButton();
        settingsButton.setText("Settings");

        c.fill = GridBagConstraints.LINE_START;
        c.weightx = 0.0;
        c.gridx = 0;
        c.gridy = 0;
        controlPanel.add(newGameButton, c);

        c.fill = GridBagConstraints.LINE_START;
        c.weightx = 0.0;
        c.gridx = 1;
        c.gridy = 0;
        controlPanel.add(refreshGamesButton, c);

        c.fill = GridBagConstraints.CENTER;
        c.weightx = 0.1;
        c.gridx = 2;
        c.gridy = 0;
        c.ipadx = 100;
        controlPanel.add(nickText, c);

        c.fill = GridBagConstraints.LINE_END;
        c.weightx = 0.0;
        c.gridx = 3;
        c.gridy = 0;
        c.ipadx = 0;
        controlPanel.add(settingsButton, c);

        /* Status bar */
        JPanel statusBar = new JPanel();
        statusBar.setBackground(Color.lightGray);
        statusBar.setLayout(new BorderLayout());

        JLabel latencyLabel = new JLabel();
        int latency = 123;         // TODO: get network latency to server
        latencyLabel.setText("Latency: " + latency + "  ");
        statusBar.add(latencyLabel, BorderLayout.LINE_END);

        /* Get Games List */
        getGamesList();

        gamesTable.setModel(tableModel);
        TableRowColorRenderer colorRenderer = new TableRowColorRenderer();
        gamesTable.setDefaultRenderer(Object.class, colorRenderer);

        JButton joinGameButton = new JButton();

        /* add join buttons to table rows */
        gamesTable.getColumn("").setCellRenderer(new ButtonRenderer());
        gamesTable.getColumn("").setCellEditor(new ButtonEditor(joinGameButton, new JCheckBox()));

        /* Resize columns in table */
        gamesTable.getColumnModel().getColumn(0).setMaxWidth(15);
        gamesTable.getColumnModel().getColumn(0).setMinWidth(15);
        gamesTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        gamesTable.getColumnModel().getColumn(1).setMinWidth(100);
        gamesTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        gamesTable.getColumnModel().getColumn(2).setMinWidth(50);
        gamesTable.getColumnModel().getColumn(3).setMaxWidth(100);
        gamesTable.getColumnModel().getColumn(3).setMinWidth(50);
        gamesTable.getColumnModel().getColumn(4).setMaxWidth(100);
        gamesTable.getColumnModel().getColumn(4).setMinWidth(50);
        gamesTable.getColumnModel().getColumn(5).setMinWidth(78);
        gamesTable.getColumnModel().getColumn(5).setMaxWidth(78);

        JScrollPane sp = new JScrollPane(gamesTable);

        /** Button action listeners*/
        newGamePrivateCheckbox.addActionListener(e -> {
            if (newGamePrivateCheckbox.isSelected()){
                newGamePassword.setEnabled(true);
            } else {
                newGamePassword.setEnabled(false);
            }

        });

        newGameConfirmButton.addActionListener(e -> {
            createNewGame(newGameName.getText(), newGamePassword.getPassword());
            controlPanel.setVisible(true);
            sp.setVisible(true);
            newGamePanel.setVisible(false);
        });

        settingsConfirmButton.addActionListener(e -> {
            try {
                Message response = conn.sendMessage(
                        new Message(MessageType.CONNECT)
                );
                IdentityResponse identityResponse = (IdentityResponse) response;
                response = conn.sendMessage(
                        new UpdateNickMessage(identityResponse.getToken(), newNickName.getText())
                );
                if (response.isError()){
                    JOptionPane.showMessageDialog(settingsConfirmButton, response.getErrorMessage());
                    return;
                }
                IdentityResponse updatedNickResponse = (IdentityResponse) response;
                playerName = updatedNickResponse.getNick();
                nickText.setText(playerName);
            } catch (IOException | ClassNotFoundException ioException) {
                ioException.printStackTrace();
            }

            controlPanel.setVisible(true);
            sp.setVisible(true);
            settingsPanel.setVisible(false);
        });

        newGameButton.addActionListener(e -> {
            controlPanel.setVisible(false);
            sp.setVisible(false);
            newGamePanel.setVisible(true);
        });

        settingsButton.addActionListener(e -> {
            controlPanel.setVisible(false);
            sp.setVisible(false);
            settingsPanel.setVisible(true);
        });

        joinGameButton.addActionListener(e -> {
            int gameID = Integer.parseInt(gamesTable.getValueAt(gamesTable.getSelectedRow(), 0).toString());
            int playerCount = Character.getNumericValue(gamesTable.getValueAt(gamesTable.getSelectedRow(), 3).toString().charAt(0));

            if (playerCount < 8){   // TODO: Actually join the game
                if (gameIsPrivate(gameID)) { // game is private
                    // show window for entering password
                    JFrame pwFrame = new JFrame("Join game");
                    pwFrame.setLayout(null);
                    pwFrame.setSize(300,150);
                    pwFrame.setVisible(true);
                    pwFrame.setLocationRelativeTo(null);
                    JLabel pwToJoinLabel = new JLabel("Enter password");
                    JPasswordField pwToJoinField = new JPasswordField();
                    JButton pwEnterGameButton = new JButton("Join game");

                    pwToJoinLabel.setBounds(100,10, 200, 20);
                    pwToJoinField.setBounds(50,30, 200, 20);
                    pwEnterGameButton.setBounds(100, 75, 95, 20);

                    pwEnterGameButton.addActionListener(e1 -> {
                        if (verifyPassword(gameID, pwToJoinField.getPassword())){
                            // join game
                            System.out.println("pw accepted, joining game " + gameID);
                            pwFrame.setVisible(false);
                        } else {
                            System.out.println("wrong password");
                            JOptionPane.showMessageDialog(joinGameButton, "Wrong password!");
                        }
                    });

                    pwFrame.add(pwToJoinLabel);
                    pwFrame.add(pwToJoinField);
                    pwFrame.add(pwEnterGameButton);
                } else { // game is not private
                    System.out.println("joining game " + gameID);
                }

            } else {
                System.out.println("game full!");
                JOptionPane.showMessageDialog(joinGameButton, "Game is full!");
            }
        });

        add(newGamePanel);
        add(settingsPanel);
        add(controlPanel, BorderLayout.PAGE_START);
        add(sp, BorderLayout.CENTER);
        add(statusBar, BorderLayout.PAGE_END);
        setVisible(true);
    }

    /**
     * Refreshes gamesList.
     * Clears the gamesTable, then fills it with updated games from server via getGamesList()
     */
    public void refreshGamesList(){
        // Remove all rows, to prevent duplicates and old games from displaying
        for (int row = tableModel.getRowCount()-1; row >= 0; row--) {
            tableModel.removeRow(row);
        }

        gameList.removeAll(gameList); // Clear gameList
        // Fill table again
        getGamesList();
        System.out.println(gameList.size());
    }

    /**
     * Fills the gamesList with the games from the server, adds them to the gamesTable
     */
    public void getGamesList(){
        System.out.println("Updating games list ...");
        try {
            Message response = null;
            response = conn.sendMessage(new Message(MessageType.CONNECT));
            if (response.isError()){
                System.out.println(response.getErrorMessage());
                return;
            }
            response = conn.sendMessage(new Message(MessageType.GET_GAME_LIST));
            if (response.isError()){
                System.out.println(response.getErrorMessage());
                return;
            }
            GameListResponse listResponse = (GameListResponse) response;
            List<GameListing> gamesFromServer = listResponse.getGameList();
            int i = 1;
            for (GameListing listing : gamesFromServer) {
                System.out.printf(
                        "%s, %s, %s, %d, %b\n",
                        listing.getID(),
                        listing.getTitle(),
                        listing.getOwner(),
                        listing.getNumberOfPlayers(),
                        listing.hasPassword()
                );
                Object[] game = {listing.getID(),listing.getTitle(),listing.getOwner(),listing.getNumberOfPlayers(),listing.hasPassword()}; // TODO: get hasStarted() from server as well
                gameList.add(game);

                String p = "No";
                if (listing.hasPassword()){
                    p = "Yes";
                }
                Object[] gameToTable = {i,listing.getTitle(), listing.getOwner(), listing.getNumberOfPlayers() + " / " + MAX_PLAYERS, p, "Join"};
                tableModel.addRow(gameToTable);
                i++;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an instance of a new game, adds it to the server. The new game will be shown in the table when refreshed.
     * @param gameName
     * @param gamePassword
     */
    public void createNewGame(String gameName, char[] gamePassword) {
        // TODO: create new game on server
        try {
            Message response = null;
            response = conn.sendMessage(new Message(MessageType.CONNECT));
            if (response.isError()){
                System.out.println(response.getErrorMessage());
                return;
            }
            response = conn.sendMessage(new NewGameMessage(
                    gameName,
                    gamePassword
            ));

            if (response.isError()){
                System.out.println(response.getErrorMessage());
                return;
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        refreshGamesList(); // refresh table
    }

    /**
     * Verify if the entered password is correct.
     * @param gameID - ID for the game the password should be checked for
     * @param enteredPassword - The entered password
     * @return - True if the entered password is correct for given game id, false if not.
     */
    public boolean verifyPassword(int gameID, char[] enteredPassword){
        /* TODO: verify password at server end, return true if correct password.
            This function will be replaced by checking responses from server instead when joining game
            (in the join button action listener)
         */

        if (enteredPassword.length == 0){   // just for testing
            return false;
        } else {
            return true; // temp, should return false by default if pw is not correct
        }
    }

    /**
     * Checks if the game is password protected. Returns true if it is protected.
     * @param gameID - ID for the game that should be checked
     * @return - returns true if the game is password protected. False if not.
     */
    public boolean gameIsPrivate(int gameID){
        // TODO: Remove. Games now have stored a value for protected or not
        // temp
        if (gamesTable.getValueAt(gamesTable.getSelectedRow(), 4).toString() == "Yes") {
            return true;
        } else {
            return false;
        }
    }
}

