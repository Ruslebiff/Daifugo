package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


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
    private String playerName = "Player 1";

    public GameLobby() {
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


        JLabel newNickNameLabel = new JLabel("Nickname: ");
        JTextField newNickName = new JTextField(playerName);
        JButton settingsConfirmButton = new JButton("Confirm");

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
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
            Object[] newGameData = {"9", newGameName.getText(), playerName, 0, newGamePrivateCheckbox.isSelected(), "Join"};
            createNewGame(newGameData);
            controlPanel.setVisible(true);
            sp.setVisible(true);
            newGamePanel.setVisible(false);
        });

        settingsConfirmButton.addActionListener(e -> {
            playerName = newNickName.getText();
            controlPanel.setVisible(true);
            sp.setVisible(true);
            settingsPanel.setVisible(false);
            nickText.setText(playerName);
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

    public void refreshGamesList(){
        // Remove all rows, to prevent duplicates and old games from displaying
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            tableModel.removeRow(row);
        }
        tableModel.removeRow(0); // jalla bugfix, removes the row that is left after the loop

        // Fill table again
        getGamesList();
    }

    public void getGamesList(){
        System.out.println("Updating games list ...");

        // Sample games - should be removed in production
        // TODO: get games list from server
        Object[][] gamesList = { // TEMPORARY
                {"1", "Game name 1", "Dr. Mundo", 2, true, "Join"},
                {"2", "Super Game For Cool Guyz", "Teemo", 6, false, "Join"},
                {"3", "Kosekroken", "Caitlyn", 8, false, "Join"},
        };

        // Add all rows
        for (int row = 0; row < gamesList.length; row++) {
            tableModel.addRow(gamesList[row]);
        }

        /* Loops through all games */
        for (int i = 0; i < tableModel.getRowCount(); i++){
            /* Info - game password protected or not */
            boolean p = ((Boolean)(gamesTable.getValueAt(i, 4)));
            if (p){
                gamesTable.setValueAt("Yes", i, 4);
            } else {
                gamesTable.setValueAt("No", i, 4);
            }

            /* Info - insert max players */
            gamesTable.setValueAt(gamesTable.getValueAt(i, 3) + " / " + MAX_PLAYERS, i, 3);
        }
    }

    /**
     * Creates an instance of a new game, adds it to the server. The new game will be shown in the table when refreshed.
     * @param newGameData - Data for the new game that is being created.
     */
    public void createNewGame(Object[] newGameData) {
        System.out.println("Creating new game ...");

        // TODO: create new game on server

        tableModel.addRow(newGameData); // should be added to server, not directly in table. This will make it crash until that is done.
        refreshGamesList(); // refresh table
    }

    /**
     * Verify if the entered password is correct.
     * @param gameID - ID for the game the password should be checked for
     * @param enteredPassword - The entered password
     * @return - True if the entered password is correct for given game id, false if not.
     */
    public boolean verifyPassword(int gameID, char[] enteredPassword){
        // TODO: verify password at server end, return true if correct password

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
        // TODO: check server if the game is protected or not
        // temp
        if (gamesTable.getValueAt(gamesTable.getSelectedRow(), 4).toString() == "Yes") {
            return true;
        } else {
            return false;
        }
    }
}

