package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.*;


public class Table extends JPanel {

    private BufferedImage image;
    private final PlayersInformation playersInformation;
    private CardsOnTable cardsOnTable;
    private GameStateTracker stateTracker;
    private Logger LOGGER;
    private JButton startBtn;
    private final int TABLE_WIDTH;
    private final int TABLE_HEIGHT;
    private final GameLobby gameLobby;
    private JLabel startString;
    private JLabel newRoundString;
    private Player player;
    private boolean wasStopped;
    private boolean wasTradePhase;


    private Void updateGUI() {

        List<Card> cards = stateTracker.getHand();
        for (Card card : cards) {
            LOGGER.info("Currently in hand: " + card.getNumber() + card.getSuit());
        }


        if (stateTracker.isCancelled()) {
            LOGGER.info("Game is cancelled, exiting...");
            gameLobby.setWaitingCursor(true);
            this.setVisible(false);
            gameLobby.showLobby(true);
            gameLobby.startHeartbeat();
            gameLobby.refreshGamesList();
            gameLobby.setWaitingCursor(false);
            return null;
        }

        if (stateTracker.isStarted() && wasStopped) {
            wasStopped = false;
            player.update(stateTracker.getHand());
            player.setVisible(true);
            startString.setVisible(false);
        }


        if (stateTracker.isTradingPhase() && !wasTradePhase) {
            wasTradePhase = true;
            player.update(stateTracker.getHand());
        }

        // setting new round text according to state
        newRoundString.setVisible(stateTracker.isTradingPhase());

        if (player != null) {
            if (wasTradePhase && !stateTracker.isTradingPhase()) {
                wasTradePhase = false;
                player.update(stateTracker.getHand());
                player.updateButtonState();
                player.setTradingPhase(false);
            }
            if (stateTracker.isStarted()) {
                player.updateButtonState();
            }
            if (stateTracker.getRoundNo() > 1 && !stateTracker.isTradingPhase())
                player.update(stateTracker.getHand());

            if (!stateTracker.isStarted()) {
                wasStopped = true;
                player.setVisible(false);
                startString.setVisible(true);
            }
        }


        playersInformation.indicateTurn();
        cardsOnTable.updateCardsOnTable();
        LOGGER.info("Gui got updated");
        return null;
    }

    public Table(int f_width, int f_height, GameStateTracker sT, GameLobby gL) {

        gameLobby = gL;

        wasStopped = true;
        wasTradePhase = false;

        LOGGER = GameLobby.LOGGER;
        this.stateTracker = sT;
        TABLE_WIDTH = f_width;
        TABLE_HEIGHT = f_height;
        try {
            image = ImageIO.read(ClientMain.class.getResourceAsStream("/green_fabric.jpg"));       // Read the image
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        setLayout(null);

        playersInformation = new PlayersInformation(stateTracker);
        int pInfoWidth = 200;
        int pInfoHeight = 100;
        playersInformation.setBounds(50,50, pInfoWidth, pInfoHeight);
        add(playersInformation);

        int cardsOnTableWidth = 300, cardsOnTableHeight = 200;
        cardsOnTable = new CardsOnTable(stateTracker);
        cardsOnTable.setBounds(
                (f_width /2) - (cardsOnTableWidth/2),
                (f_height /3) - (cardsOnTableHeight/2),
                   cardsOnTableWidth, cardsOnTableHeight
        );
        add(cardsOnTable);

        stateTracker.registerConnectionLostCallback(() -> {
            gameLobby.quitClient();
            return null;
        });
        stateTracker.registerCallback(this::updateGUI);

        if(stateTracker.isOwner()) {
            startBtn = new JButton("Start");
            startBtn.setBounds(f_width-150,50, 100,50);
            add(startBtn);
            startBtn.addActionListener(e -> startGame());
        }

        JButton exitButton = new JButton("Exit");
        exitButton.setBounds(f_width-150, 100, 100, 50);
        add(exitButton);
        exitButton.addActionListener(e -> exitGame());

        startString = new JLabel("Waiting for game to start");
        startString.setBounds((f_width/2) - 120, 100, 250,50);
        startString.setFont(new Font("Sans Serif", Font.BOLD, 15));
        add(startString);


        newRoundString = new JLabel("Trading phase");
        newRoundString.setBounds((f_width/2) - 120, 100, 250,50);
        newRoundString.setFont(new Font("Sans Serif", Font.BOLD, 24));
        newRoundString.setForeground(Color.YELLOW);
        newRoundString.setVisible(false);
        add(newRoundString);


        player = new Player(TABLE_WIDTH/2, stateTracker);
        player.setBounds((TABLE_WIDTH/2) - ((TABLE_WIDTH/2)/2) - 25,
                (TABLE_HEIGHT/2) + 100,
                TABLE_WIDTH/2,
                (TABLE_HEIGHT/8) + 100);
        add(player);
        player.setVisible(false);

        gL.setWaitingCursor(false);
    }

    private void exitGame() {
        gameLobby.setWaitingCursor(true);
        if (stateTracker.isOwner())
            stateTracker.cancelGame();
        else
            stateTracker.leaveGame();
        this.setVisible(false);
        gameLobby.showLobby(true);
        gameLobby.startHeartbeat();
        gameLobby.refreshGamesList();
        gameLobby.setWaitingCursor(false);
    }

    public void startGame() {
        if(startBtn.getText().equals("Start")) {
            LOGGER.info("Entered buttonlistener");
            if (!stateTracker.startGame()) {
                JOptionPane.showMessageDialog(
                        this,
                        "A game must have at least 3 players to start.",
                        "Unable to start game",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            startBtn.setText("Stop");
            repaint();
        } else {
            startBtn.setText("Start");
            stateTracker.stopGame();
/*
            this.setVisible(false);
            gameLobby.showLobby(true);
            gameLobby.startHeartbeat();
            gameLobby.setWaitingCursor(false);
*/
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this); // Draws the image of onto the JPanel
    }
}
