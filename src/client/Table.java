package client;

import common.Role;

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
    private JLabel statusString;
    private Player player;
    private boolean wasStopped;
    private boolean wasTradePhase;
    private final JTextArea cardsInPlayCounter;

    private final String yourTurn = "It's your turn to play cards. Select cards to play.";
    private final String bumTwoTrade = "You must give your 2 best cards to the President.";
    private final String bumOneTrade = "You must give your best card to the President.";
    private final String viceBumTrade = "You must give your best card to the Vice-President.";
    private final String vicePresidentTrade = "Please choose one card to give to the Vice-Bum.";
    private final String presidentOneTrade = "Please choose any one card to give to the Bum.";
    private final String presidentTwoTrade = "Please choose any two cards to give to the Bum.";
    private final String neutralTrade = "Please be patient while the rich and poor trades cards.";

    private final String waitingForPlayersString = "Waiting for more players...";
    private final String waitingForGameStartString = "Waiting for game to start";


    private Void updateGUI() {

        if (statusString != null)
            statusString.setVisible(false);

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


        if (stateTracker.isTradingPhase() && !wasTradePhase && player != null) {
            wasTradePhase = true;
            player.update(stateTracker.getHand());
        }

        if (stateTracker.isTradingPhase()) {
            if (stateTracker.iHaveToTrade()) {
                switch (stateTracker.getRole()) {
                    case PRESIDENT -> statusString.setText(
                            (stateTracker.getMyRoleNumber() == 2) ? presidentTwoTrade : presidentOneTrade
                    );
                    case BUM -> statusString.setText(
                            (stateTracker.getMyRoleNumber() == -2) ? bumTwoTrade : bumOneTrade
                    );
                    case VICE_BUM -> statusString.setText(viceBumTrade);
                    case VICE_PRESIDENT -> statusString.setText(vicePresidentTrade);
                }
            } else {
                statusString.setText(neutralTrade);
            }
            statusString.setVisible(true);
        }

        int amountOfPlayers = stateTracker.getPlayerList().size();

        if (startString != null) {
            if (amountOfPlayers < 3)
                startString.setText(waitingForPlayersString);
            else
                startString.setText(waitingForGameStartString);
        }

        if (startBtn != null) {
            if (amountOfPlayers < 3) {
                startBtn.setEnabled(false);
            } else {
                startBtn.setEnabled(true);
            }

            if (!stateTracker.isStarted()) {
                startBtn.setText("Start");
            } else {
                startBtn.setText("Stop");
            }
        }


        cardsInPlayCounter.setVisible(stateTracker.getCardsInPlay() != 0);
        String inPlay = "";
        switch(stateTracker.getCardsInTrick()) {
            case 1 -> inPlay = "SINGLES!";
            case 2 -> inPlay = "DOUBLES!";
            case 3 -> inPlay = "TRIPLES!";
            default -> inPlay = "";
        }

        if(stateTracker.isNewTrick()){
            LOGGER.info("Inside new trick " + stateTracker.getLastTrick());
            switch(stateTracker.getLastTrick()) {

                case FOUR_SAME -> inPlay = "FOUR OF A KIND!";
                case THREE_CLUBS -> inPlay = "THREE OF CLUBS!";
                case ALL_PASS -> inPlay = "EVERYBODY PASSED!";
                default -> inPlay = "";
            }
            cardsInPlayCounter.setVisible(true);

        }
        cardsInPlayCounter.setText(inPlay);

        // setting new round text according to state
        if (newRoundString != null)
            newRoundString.setVisible(stateTracker.isTradingPhase());

        if (player != null) {
            if (player.isGoneOut()) {
                player.setGoneOut(false);
                LOGGER.info("Player has gone out");

                // calculate which role we will get
                int roleNum = stateTracker.getGoneOutNumber();
                int playerNum = stateTracker.getPlayerList().size();
                String newRole;

                if (roleNum == 1)
                    newRole = "President";
                else if (roleNum == 2 && playerNum > 3)
                    newRole = "Vice-President";
                else if (roleNum == playerNum-2 && playerNum >3)
                    newRole = "Vice-Bum";
                else if (roleNum == playerNum)
                    newRole = "Bum";
                else
                    newRole = "Neutral";

                JOptionPane.showMessageDialog(
                        this,
                        "You're out of the round! Your new role will be: " + newRole,
                        "You've gone out!",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

            if (wasTradePhase && !stateTracker.isTradingPhase()) {
                wasTradePhase = false;
                player.update(stateTracker.getHand());
                player.updateButtonState();
                player.setTradingPhase(false);
            }
            if (stateTracker.isStarted()) {
                player.updateButtonState();
                if (stateTracker.isMyTurn()) {
                    statusString.setText(yourTurn);
                    statusString.setVisible(true);
                }
            }

            if (!stateTracker.isStarted()) {
                wasStopped = true;
                player.setVisible(false);
                statusString.setVisible(false);
                newRoundString.setVisible(false);
                cardsInPlayCounter.setVisible(false);
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
            image = ImageIO.read(
                    ClientMain.class.getResourceAsStream("/green_fabric.jpg")    // Read the image
            );
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
        cardsOnTable = new CardsOnTable(stateTracker, cardsOnTableWidth, cardsOnTableHeight);
        cardsOnTable.setBounds(
                (f_width /2) - (cardsOnTableWidth/2) - 25,
                (f_height /3) - (cardsOnTableHeight/2),
                   cardsOnTableWidth, cardsOnTableHeight
        );
        add(cardsOnTable);

        Color westernYellow = new Color(0xFFDFCE25, true);
        Color daifugoBlue = new Color(0xFF4988FF, true);

        cardsInPlayCounter = new JTextArea();
        cardsInPlayCounter.setLineWrap(true);
        cardsInPlayCounter.setWrapStyleWord(true);
        cardsInPlayCounter.setEditable(false);
        cardsInPlayCounter.setOpaque(false);
        cardsInPlayCounter.setBounds((f_width/2) + (f_width/5), (cardsOnTableHeight) + (cardsOnTableHeight/2) - 25,200,200);
        cardsInPlayCounter.setFont(gameLobby.westernFont.deriveFont(Font.BOLD, 50));
        cardsInPlayCounter.setForeground(westernYellow);
        add(cardsInPlayCounter);



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

        startString = new JLabel(waitingForPlayersString, SwingConstants.CENTER);
        startString.setBounds((f_width/2) - 175, (f_height/2)-50, 350,50);
        startString.setFont(gameLobby.westernFont.deriveFont(Font.BOLD, 40));
        startString.setForeground(daifugoBlue);
        add(startString);


        newRoundString = new JLabel("Trading phase", SwingConstants.CENTER);
        newRoundString.setBounds((f_width/2) - 200, (f_height/2)-80, 400,80);
        newRoundString.setFont(gameLobby.westernFont.deriveFont(Font.PLAIN, 72));
        newRoundString.setForeground(daifugoBlue);
        newRoundString.setVisible(false);
        add(newRoundString);

        statusString = new JLabel("Some testing text that doesn't matter", SwingConstants.CENTER);
        statusString.setBounds((f_width/2) - 250, (f_height/2)+50, 500,50);
        statusString.setFont(gameLobby.normalFont.deriveFont(Font.BOLD, 18));
        statusString.setForeground(westernYellow);
        statusString.setVisible(false);
        add(statusString);


        player = new Player(TABLE_WIDTH/2, stateTracker);
        player.setBounds((TABLE_WIDTH/2) - ((TABLE_WIDTH/2)/2),
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
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this); // Draws the image of onto the JPanel
    }
}
