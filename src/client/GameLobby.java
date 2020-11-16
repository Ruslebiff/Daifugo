package client;

import client.networking.ClientConnection;
import common.GameListing;
import protocol.*;
import server.exceptions.UserSessionError;

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
    private final int window_height = 1000;
    private final int window_width = 1000;
    private final int MAX_PLAYERS = 8;
    private String playerName;
    private final List<GameListing> gameList = new ArrayList<>();
    private ClientConnection conn = null;
    private int latency = 0;
    private String serverAddress = "localhost"; // Default server address, will be changed through settings etc
    private volatile boolean connectionOK = false;
    private JTextField newServerAddressTextField = new JTextField(serverAddress);
    private ScheduledExecutorService heartbeatExecutor;
    private JLabel latencyLabel = new JLabel();

    /* Lobby Panels */
    JPanel newGamePanel = new JPanel();
    JPanel statusBar = new JPanel();
    JPanel settingsPanel = new JPanel();
    JPanel controlPanel = new JPanel();
    JScrollPane sp = new JScrollPane(gamesTable);

    public GameLobby() {

        try {
            conn = new ClientConnection(serverAddress);
            connectionOK = true;
        } catch (IOException e) {
            System.out.println("ERROR: Failed to connect!");
            connectToServerFrame();
        }

        while(!connectionOK){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Connected to server " + serverAddress);

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


        /**
         *  New game view
         ************************/
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


        /**
         *  Settings view
         ************************/
        settingsPanel.setSize(window_width,window_height);
        settingsPanel.setVisible(false);
        settingsPanel.setLayout(null);


        JLabel newNickNameLabel = new JLabel("Nickname: ");
        newNickNameLabel.setBounds(100, 20, 100,20);

        JTextField newNickNameTextField = new JTextField(playerName);
        newNickNameTextField.setBounds(200, 20, 150,20);

        JLabel newServerAddressLabel = new JLabel("Server address: ");
        newServerAddressLabel.setBounds(100, 50, 100,20);

        newServerAddressTextField.setBounds(200, 50, 150,20);

        JLabel settingsConnectionFailedMessage = new JLabel("Connection failed");
        settingsConnectionFailedMessage.setForeground(new Color(255, 0, 0));
        settingsConnectionFailedMessage.setBounds(400, 50, 150,20);
        settingsConnectionFailedMessage.setVisible(false);


        JButton settingsConfirmButton = new JButton("Confirm");
        settingsConfirmButton.setBounds(getWidth()/2 - 75, getHeight() - 100, 150,40);



        settingsPanel.add(newNickNameLabel, gbc);
        settingsPanel.add(newNickNameTextField, gbc);
        settingsPanel.add(newServerAddressLabel, gbc);
        settingsPanel.add(newServerAddressTextField, gbc);
        settingsPanel.add(settingsConfirmButton, gbc);

        /**
         *  Control bar
         ************************/

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

        /**
         *  Status bar
         ************************/
        statusBar.setBackground(Color.lightGray);
        statusBar.setLayout(new BorderLayout());


        startHeartbeat();
        latencyLabel.setText("Latency: " + latency + "  ");
        statusBar.add(latencyLabel, BorderLayout.LINE_END);
        /***********************/


        /**
         *  Table
         ************************/
        getGamesList();

        gamesTable.setModel(tableModel);
        TableRowColorRenderer colorRenderer = new TableRowColorRenderer();
        gamesTable.setDefaultRenderer(Object.class, colorRenderer);
        gamesTable.setAutoCreateRowSorter(true);

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



        /**
         *  Button action listeners
         ************************/
        newGamePrivateCheckbox.addActionListener(e -> {
            newGamePassword.setEnabled(newGamePrivateCheckbox.isSelected());

        });

        newGameConfirmButton.addActionListener(e -> {
            char[] pw;
            if (newGamePrivateCheckbox.isSelected() && newGamePassword.getPassword().length > 0){
                pw = newGamePassword.getPassword();
            } else {
                pw = null;
            }
            createNewGame(newGameName.getText(), pw);
            sp.setVisible(false);
        });

        settingsConfirmButton.addActionListener(e -> {
            boolean allSettingsOK;
            if (!newServerAddressTextField.getText().equals(serverAddress)) {
                serverAddress = newServerAddressTextField.getText();
                connectionOK = false;
                try {
                    conn = new ClientConnection(serverAddress);
                    connectionOK = true;
                } catch (IOException a) {
                    System.out.println("ERROR: Failed to connect!");
                    connectToServerFrame();
                }
            }
            allSettingsOK = connectionOK;

            if (!newNickNameTextField.getText().equals(playerName)){
                try {
                    Message response = conn.sendMessage(
                            new Message(MessageType.CONNECT)
                    );
                    IdentityResponse identityResponse = (IdentityResponse) response;
                    response = conn.sendMessage(
                            new UpdateNickMessage(identityResponse.getToken(), newNickNameTextField.getText())
                    );
                    if (response.isError()){
                        JOptionPane.showMessageDialog(settingsConfirmButton, response.getErrorMessage());
                        allSettingsOK = false;
                        return;
                    }
                    IdentityResponse updatedNickResponse = (IdentityResponse) response;
                    playerName = updatedNickResponse.getNick();
                    nickText.setText(playerName);
                } catch (IOException | ClassNotFoundException ioException) {
                    ioException.printStackTrace();
                }
            }

            if (allSettingsOK){ // settings ok, exit settings view
                showLobby(true);
            }

        });

        newGameButton.addActionListener(e -> {
            showLobby(false);
            newGamePanel.setVisible(true);
        });

        settingsButton.addActionListener(e -> {
            showLobby(false);
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

                            // TODO: Joining game does not launch game view
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
                System.exit(0);
            }
        });

        add(newGamePanel, 0);
        add(settingsPanel, 0);
        add(controlPanel, BorderLayout.PAGE_START, 0);
        add(sp, BorderLayout.CENTER, 0);
        add(statusBar, BorderLayout.PAGE_END, 0);
        setVisible(true);
        setLocationRelativeTo(null);
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
                System.out.printf(
                        "%s, %s, %s, %d, %b\n",
                        listing.getID(),
                        listing.getTitle(),
                        listing.getOwner(),
                        listing.getNumberOfPlayers(),
                        listing.hasPassword()
                );
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
     * Creates an instance of a new game, adds it to the server. The new game will be shown in the table when
     * refreshed.
     * @param gameName The name of the game that should be created.
     * @param gamePassword The password for the game that should be created. Use null if it shouldn't have any password.
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


            heartbeatExecutor.shutdown();
            GameStateResponse tmp = (GameStateResponse) response;
            GameStateTracker tracker = new ServerTracker(
                    conn,
                    tmp.getState()
            );

            showLobby(false);
            setTitle("Daifugo - " + gameName);

            Table playTable = new Table(window_width, window_height, tracker, this);
            playTable.setBounds(0,0, getWidth(), getHeight());
            playTable.setVisible(true);
            playTable.setBounds(0,0,window_width,window_height);
            add(playTable, 1);
            playTable.repaint();
            playTable.revalidate();





        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
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

    /**
     * A popup frame that informs that the connection has failed, and provides a text field to enter a new address.
     * When clicking the confirm button, a new connection to the server is established. If this fails, the frame re-appears
     * for another try until a connection is successful.
     */
    public void connectToServerFrame() {
        JFrame connectionFrame = new JFrame();
        connectionFrame.setLayout(null);
        connectionFrame.setSize(400,200);

        JLabel connectionFailedMessage = new JLabel("Connection failed");
        connectionFailedMessage.setForeground(new Color(255, 0, 0));
        connectionFailedMessage.setBounds(connectionFrame.getWidth()/2 - 50, 0, 100, 50);

        JLabel enterServerAddressMessage = new JLabel("Please enter a valid server address");
        enterServerAddressMessage.setBounds(connectionFrame.getWidth()/2 - 102, 20, 300, 50);


        JTextField addressTextArea = new JTextField(serverAddress);
        addressTextArea.setEditable(true);
        addressTextArea.setBounds( connectionFrame.getWidth()/2 - 100, connectionFrame.getHeight()/3, 200,20);


        JButton confirmButton = new JButton("Confirm");
        confirmButton.setBounds(connectionFrame.getWidth()/2 - 50, connectionFrame.getHeight()/2, 100, 40);
        confirmButton.addActionListener(a -> {
            serverAddress = addressTextArea.getText();
            try {
                conn = new ClientConnection(serverAddress);
                Message response;
                response = conn.sendMessage(new Message(MessageType.CONNECT));
                if (response.isError()){
                    System.out.println("ERROR: " + response.getErrorMessage());
                } else {
                    connectionOK = true;
                    newServerAddressTextField.setText(serverAddress);
                    connectionFrame.dispose(); // destroy frame
                }
            } catch (IOException | ClassNotFoundException e) {
                connectionFrame.dispose(); // destroy frame
                connectToServerFrame();   // make new frame, try again
            }
        });

        /* Handle close button */
        connectionFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        connectionFrame.add(connectionFailedMessage);
        connectionFrame.add(enterServerAddressMessage);
        connectionFrame.add(addressTextArea);
        connectionFrame.add(confirmButton);
        connectionFrame.setLocationRelativeTo(null);
        connectionFrame.setVisible(true);

    }

    /**
     * Resets lobby view to default view if true.
     * Hides all lobby elements if false.
     * @param b reset lobby view if true, hide all lobby elements if false
     */
    public void showLobby(boolean b){
        if (b){
            sp.setVisible(true);
            statusBar.setVisible(true);
            newGamePanel.setVisible(false);
            settingsPanel.setVisible(false);
            controlPanel.setVisible(true);
        } else {
            sp.setVisible(false);
            statusBar.setVisible(false);
            newGamePanel.setVisible(false);
            settingsPanel.setVisible(false);
            controlPanel.setVisible(false);
        }
    }

    public void startHeartbeat(){
        Runnable latencyRunnable = () -> {
            latency = getLatency();
            latencyLabel.setText("Latency: " + latency + "  ");
        };
        heartbeatExecutor = Executors.newScheduledThreadPool(1);
        heartbeatExecutor.scheduleAtFixedRate(latencyRunnable, 1, 1, TimeUnit.SECONDS);
    }
}

