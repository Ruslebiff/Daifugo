package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;


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

    public GameLobby() {

        String playerName = "Player 1";

        /* Create window */
        setSize(window_width,window_height);
        setLayout(new BorderLayout());
        setTitle("Daifugo - Lobby");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        /* New game view */
        JPanel newGamePanel = new JPanel();
        newGamePanel.setVisible(false);

        /* Settings view */


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
        settingsButton.addActionListener(e -> {
            editSettings();
        });

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
        newGameButton.addActionListener(e -> {
            controlPanel.setVisible(false);
            sp.setVisible(false);

            newGamePanel.setVisible(true);




            Object[] tempNewGameData = {"1", "Game name 1", "Dr. Mundo", 2, true, "Join"};
            createNewGame(tempNewGameData);

        });

        joinGameButton.addActionListener(e -> {
            int gameID = Integer.parseInt(gamesTable.getValueAt(gamesTable.getSelectedRow(), 0).toString());
            int playerCount = Character.getNumericValue(gamesTable.getValueAt(gamesTable.getSelectedRow(), 3).toString().charAt(0));

            if (playerCount < 8){   // TODO: Actually join the game
                System.out.println("joining game " + gameID);
            } else {
                System.out.println("game full!");
                JOptionPane.showMessageDialog(joinGameButton, "Game is full!");
            }
        });

        add(newGamePanel);
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
        // TODO: Sample games, get from server instead
        System.out.println("Updating games list ...");


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

    // TODO: BUTTON - New game - Functionality
    public void createNewGame(Object[] newGameData) {
        System.out.println("Creating new game ...");
        // use newGameData to add the new game on server


//        tableModel.addRow(row);
    }

    // TODO: BUTTON - Settings - Functionality
    public void editSettings() {
        System.out.println("Editing settings ...");
    }
}

