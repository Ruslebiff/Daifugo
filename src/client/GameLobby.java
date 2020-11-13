package client;

import client.networking.ClientConnection;
import common.GameListing;
import protocol.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// TODO: a filter box to search through available games
// TODO: list of games can be sorted in different ways
// TODO: an option for using a non-default server address is available

public class GameLobby extends JFrame {
    private final String[] columnNames = {
            "ID",
            "Game name",
            "Owner",
            "Players",
            "Private",
            ""
    };
    private final DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 5; // only last column, needed for button to work
        }
    };
    private final JTable gamesTable = new JTable(tableModel);
    private int window_height = 1000;
    private int window_width = 1000;
    private int MAX_PLAYERS = 8;
    private String playerName;
    private final List<GameListing> gameList = new ArrayList<>();
    private ClientConnection conn = null;
    private int latency = 0;

    public GameLobby() {
        try {
            conn = new ClientConnection("localhost");
        } catch (IOException e) {
            System.out.println("ERROR: Failed to connect!");
            e.printStackTrace();
        }

        try {
            Message response = conn.sendMessage(new Message(MessageType.CONNECT));
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
        Runnable latencyRunnable = () -> {
            latency = getLatency();
            latencyLabel.setText("Latency: " + latency + "  ");
        };
        ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(1);
        heartbeatExecutor.scheduleAtFixedRate(latencyRunnable, 1, 1, TimeUnit.SECONDS);


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
        gamesTable.getColumnModel().getColumn(0).setMaxWidth(25);
        gamesTable.getColumnModel().getColumn(0).setMinWidth(25);
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

        /* Button action listeners*/
        newGamePrivateCheckbox.addActionListener(e -> {
            newGamePassword.setEnabled(newGamePrivateCheckbox.isSelected());

        });

        newGameConfirmButton.addActionListener(e -> {
            char[] pw;
            if (newGamePrivateCheckbox.isSelected()){
                pw = newGamePassword.getPassword();
            } else {
                pw = new char[0];
            }
            createNewGame(newGameName.getText(), pw);
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
            int gameNumber = Integer.parseInt(gamesTable.getValueAt(gamesTable.getSelectedRow(), 0).toString());
            int playerCount = Character.getNumericValue(gamesTable.getValueAt(gamesTable.getSelectedRow(), 3).toString().charAt(0));

            if (playerCount < 8){
                if (gamesTable.getValueAt(gamesTable.getSelectedRow(), 4).toString().equals("Yes")) { // game is private // TODO: can we use the hasPassword() instead?
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

                        try {
                            Message response = conn.sendMessage(
                                    new Message(MessageType.CONNECT)
                            );
                            if (response.isError()){
                                System.out.println("ERROR: " + response.getErrorMessage());
                                return;
                            }

                            String gameID;
                            gameID = gameList.get(gameNumber-1).getID();
                            System.out.println("Entering game with ID: " + gameID);

                            response = conn.sendMessage(new JoinGameRequest(gameID, pwToJoinField.getPassword()));
                            if (response.isError()){
                                if(response.getMessageType() == MessageType.PASSWORD_ERROR){
                                    JOptionPane.showMessageDialog(joinGameButton, "Wrong password!");
                                }
                                System.out.println("ERROR: " + response.getErrorMessage());
                            }

                        } catch (IOException | ClassNotFoundException ioException) {
                            ioException.printStackTrace();
                        }


                    });

                    pwFrame.add(pwToJoinLabel);
                    pwFrame.add(pwToJoinField);
                    pwFrame.add(pwEnterGameButton);
                } else { // game is not private
                    System.out.println("joining game " + gameNumber);
                }

            } else {
                System.out.println("game full!");
                JOptionPane.showMessageDialog(joinGameButton, "Game is full!");
            }
        });

        /* Handle close window */
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                heartbeatExecutor.shutdown();    // kill latency thread
                try {
                    Message response = conn.sendMessage(new Message(MessageType.DISCONNECT));
                    if (response.isError()){
                        System.out.println("ERROR: " + response.getErrorMessage());
                    }

                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
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
    }

    /**
     * Fills the gamesList with the games from the server, adds them to the gamesTable
     */
    public void getGamesList(){
        System.out.println("Updating games list ...");
        try {
            Message response;
            response = conn.sendMessage(new Message(MessageType.CONNECT));
            if (response.isError()){
                System.out.println("ERROR: " + response.getErrorMessage());
                return;
            }
            response = conn.sendMessage(new Message(MessageType.GET_GAME_LIST));
            if (response.isError()){
                System.out.println("ERROR: " + response.getErrorMessage());
                return;
            }
            GameListResponse listResponse = (GameListResponse) response;
            List<GameListing> gamesFromServer = listResponse.getGameList();
            int i = 1;
            for (GameListing listing : gamesFromServer) {
//                System.out.printf(
//                        "%s, %s, %s, %d, %b\n",
//                        listing.getID(),
//                        listing.getTitle(),
//                        listing.getOwner(),
//                        listing.getNumberOfPlayers(),
//                        listing.hasPassword() // TODO: this is always true :(
//                );
                GameListing game = new GameListing(listing.getID(), listing.getTitle(), listing.getOwner(), listing.getNumberOfPlayers(), listing.hasPassword(), listing.hasStarted());
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
     * @param gameName The name of the game that should be created.
     * @param gamePassword The password for the game that should be created. Leave empty char[] if it shouldn't have any password.
     */
    public void createNewGame(String gameName, char[] gamePassword) {
        try {
            Message response;
            response = conn.sendMessage(new Message(MessageType.CONNECT));
            if (response.isError()){
                System.out.println("ERROR: " + response.getErrorMessage());
                return;
            }
            response = conn.sendMessage(new NewGameMessage(
                    gameName,
                    gamePassword
            ));

            if (response.isError()){
                System.out.println("ERROR: " + response.getErrorMessage());
                return;
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        refreshGamesList(); // refresh table
    }

    /**
     * Sends a HeartbeatMessage to the server and calculates the time it took.
     * @return the time between your heartbeat request was sent from you and received by the server.
     */
    public int getLatency(){
        int l = 0;
        long timestampBefore = Instant.now().toEpochMilli();

        try {
            Message response;
            response = conn.sendMessage(new Message(MessageType.CONNECT));
            if (response.isError()){
                System.out.println("ERROR: " + response.getErrorMessage());
                return 0;
            }
            HeartbeatMessage heartbeatResponse = (HeartbeatMessage) conn.sendMessage(
                    new HeartbeatMessage(timestampBefore)
            );
            if (heartbeatResponse.isError()){
                System.out.println("ERROR: " + response.getErrorMessage());
                return 0;
            }

            l = (int) (heartbeatResponse.getTime() - timestampBefore);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return l;
    }
}

