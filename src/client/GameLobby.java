package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;


public class GameLobby extends JFrame {
    public GameLobby() {
        int window_height = 1000;
        int window_width = 1000;
        int MAX_PLAYERS = 8;

        /* Create window */
        setSize(window_width,window_height);
        setLayout(new BorderLayout());
        setTitle("Daifugo - Lobby");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        /* Control bar */
        JPanel controlPanel = new JPanel();
        controlPanel.setBounds(0, 0, window_width, window_height/20);
        controlPanel.setBackground(Color.lightGray);
        // TODO: buttons for new game etc
        JButton testButton = new JButton();
        testButton.setBounds(50, 50, 150, 40);
        testButton.setText("Test Button");
        controlPanel.add(testButton);

        /* Lobby table */
        // TODO: get games from server
        // TODO: Column with lock icon for password protected games?
        String[] columnNames = {
                "ID",
                "Game name",
                "Owner",
                "Players",
                "Private",
                ""
        };

        JButton joinGameButton = new JButton();
        joinGameButton.setText("Join");

        // TODO: Sample games, get from server instead
        Object[][] games = {
                {"1", "Game name 1", "Dr. Mundo", 2, true, joinGameButton.getText()},
                {"2", "Super Game For Cool Guyz", "Teemo", 6, false, joinGameButton.getText()},
                {"3", "Kosekroken", "Caitlyn", 8, false, joinGameButton.getText()},
        };


        JTable gamesTable = new JTable(games, columnNames);
        DefaultTableModel tableModel = new DefaultTableModel(games, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // only last column, needed for button to work
            }
        };

        gamesTable.setModel(tableModel);
        TableRowColorRenderer colorRenderer = new TableRowColorRenderer();
        gamesTable.setDefaultRenderer(Object.class, colorRenderer);

        joinGameButton.addActionListener(e -> {
            int gameID = Integer.parseInt(gamesTable.getValueAt(gamesTable.getSelectedRow(), 0).toString());
            int playerCount = Character.getNumericValue(games[gameID-1][3].toString().charAt(0));

            if (playerCount < 8){   // TODO: Actually join the game
                System.out.println("joining game " + gameID);
            } else {
                System.out.println("game full!");
                JOptionPane.showMessageDialog(joinGameButton, "Game is full!");
            }
        });

        /* add join buttons to table rows */
        gamesTable.getColumn("").setCellRenderer(new ButtonRenderer());
        gamesTable.getColumn("").setCellEditor(new ButtonEditor(joinGameButton, new JCheckBox()));

        /* Resize columns in table */
        gamesTable.getColumnModel().getColumn(0).setMaxWidth(15);
        gamesTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        gamesTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        gamesTable.getColumnModel().getColumn(3).setMaxWidth(100);
        gamesTable.getColumnModel().getColumn(4).setMaxWidth(100);
        gamesTable.getColumnModel().getColumn(5).setMaxWidth(100);

        /* Loops through all games */
        for (int i = 0; i < games.length; i++){
            /* Info - game password protected or not */
            boolean p = ((Boolean)(games[i][games[i].length - 2]));
            if (p){
                gamesTable.setValueAt("Yes", i, 4);
            } else {
                gamesTable.setValueAt("No", i, 4);
            }

            /* Info - insert max players */
            gamesTable.setValueAt(gamesTable.getValueAt(i, 3) + " / " + MAX_PLAYERS, i, 3);
        }

        JScrollPane sp = new JScrollPane(gamesTable);
        add(controlPanel, BorderLayout.PAGE_START);
        add(sp, BorderLayout.CENTER);
        setVisible(true);
    }
}

