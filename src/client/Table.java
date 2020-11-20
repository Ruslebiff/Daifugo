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
    private boolean gotMessage = false;


    private Void updateGUI() {

        if (startBtn != null) {
            if (!stateTracker.isStarted() && stateTracker.getPlayerList().size() < 3) {
                startBtn.setEnabled(false);
            }
            else
                startBtn.setEnabled(true);
        }

        if (statusString != null)
            statusString.setVisible(false);

        List<Card> cards = stateTracker.getHand();

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
            if (player != null) {
                player.update(stateTracker.getHand());
                player.setVisible(true);
            }
            startString.setVisible(false);
        }


        if (stateTracker.isTradingPhase() && !wasTradePhase) {
            wasTradePhase = true;
            player.update(stateTracker.getHand());
        }

        if (stateTracker.isTradingPhase()) {
            if (!gotMessage && stateTracker.getGoneOutNumber() == stateTracker.getPlayerList().size()) {
                gotMessage = true;

                JOptionPane.showMessageDialog(
                        this,
                        "You're out of the round! Your new role will be: Bum",
                        "You've gone out!",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
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
            if (!player.isGoneOut() && gotMessage && !stateTracker.isTradingPhase())
                gotMessage = false;

            if (player.isGoneOut() && !gotMessage) {
                player.setGoneOut(false);
                gotMessage = true;
                LOGGER.info("Player has gone out");

                // calculate which role we will get
                int goneOutNumber = stateTracker.getGoneOutNumber();
                int playerNum = stateTracker.getPlayerList().size();
                String newRole;

                if (goneOutNumber == 1)
                    newRole = "President";
                else if (goneOutNumber == 2 && playerNum > 3)
                    newRole = "Vice-President";
                else if (goneOutNumber == playerNum-1 && playerNum > 3) {
                    newRole = "Vice-Bum";
                }
                else if (goneOutNumber == playerNum)
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

        playersInformation = new PlayersInformation(stateTracker, gameLobby);
        int pInfoWidth = 200;
        int pInfoHeight = 100;
        playersInformation.setBounds(50,50, pInfoWidth, pInfoHeight);
        add(playersInformation);

        int cardsOnTableWidth = 230, cardsOnTableHeight = 120;
        int cardsOnTableX = (f_width /2) - (cardsOnTableWidth/2);
        int cardsOnTableY = (f_height /2) - (cardsOnTableHeight/2) - 60;

        cardsOnTable = new CardsOnTable(stateTracker, cardsOnTableWidth, cardsOnTableHeight);
//        cardsOnTable.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        cardsOnTable.setBounds(
                cardsOnTableX,
                cardsOnTableY,
                cardsOnTableWidth,
                cardsOnTableHeight
        );
        add(cardsOnTable);

        Color westernYellow = new Color(0xdfce25);
        Color westernRed = new Color(0x652010);
        Color daifugoBlue = new Color(0x4988ff);

        cardsInPlayCounter = new JTextArea();
        cardsInPlayCounter.setLineWrap(true);
        cardsInPlayCounter.setWrapStyleWord(true);
        cardsInPlayCounter.setEditable(false);
        cardsInPlayCounter.setOpaque(false);
        cardsInPlayCounter.setBounds((f_width/2) + (f_width/5), cardsOnTableY,200,200);
        cardsInPlayCounter.setFont(gameLobby.westernFont.deriveFont(Font.BOLD, 50));
        cardsInPlayCounter.setForeground(westernYellow);
//        cardsInPlayCounter.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
        add(cardsInPlayCounter);



        stateTracker.registerConnectionLostCallback(() -> {
            gameLobby.quitClient();
            return null;
        });
        stateTracker.registerCallback(this::updateGUI);


        JButton exitButton = new JButton("Exit");
        exitButton.setBounds(f_width-150, 50, 100, 50);
        add(exitButton);
        exitButton.addActionListener(e -> exitGame());

        if(stateTracker.isOwner()) {
            startBtn = new JButton("Start");
            startBtn.setBounds(f_width-150,100, 100,50);
            startBtn.setEnabled(false);
            add(startBtn);
            startBtn.addActionListener(e -> startGame());
        }

        startString = new JLabel("Waiting for game to start");
        startString.setBounds((f_width/2) - 150, (f_height/2)-50, 300,50);
        startString.setFont(gameLobby.westernFont.deriveFont(Font.BOLD, 40));
//        startString.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        startString.setForeground(daifugoBlue);
        add(startString, 0);


        newRoundString = new JLabel("Trading phase", SwingConstants.CENTER);
        newRoundString.setBounds((f_width/2) - 200, (f_height/2)-80, 400,80);
        newRoundString.setFont(gameLobby.westernFont.deriveFont(Font.PLAIN, 72));
        newRoundString.setForeground(daifugoBlue);
        newRoundString.setVisible(false);
        add(newRoundString, 0);

        statusString = new JLabel("Some testing text that doesn't matter", SwingConstants.CENTER);
        statusString.setBounds((f_width/2) - 250, (f_height/2)+50, 500,50);
        statusString.setFont(gameLobby.normalFont.deriveFont(Font.BOLD, 18));
        statusString.setForeground(westernYellow);
        statusString.setVisible(false);
        add(statusString,0);


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

    public void showScoreBoard() {
        if(stateTracker.getPlayerList().size() >= 3) {
            int scoreBoardHeight = (stateTracker.getPlayerList().size() * 20 + 60);
            int scoreBoardWidth = 400;
            JFrame scoreFrame = new JFrame("Scoreboard");
            ScoreBoard scoreBoard = new ScoreBoard(stateTracker, gameLobby, scoreBoardWidth);
            scoreBoard.setBounds(0,0,scoreBoardWidth,scoreBoardHeight);
            scoreFrame.setSize(scoreBoard.getWidth(), scoreBoard.getHeight());
            scoreFrame.add(scoreBoard);
            scoreFrame.setLocationRelativeTo(null);          // Place in middle of screen
            scoreFrame.setVisible(true);                     // Frame visible
        } else
            JOptionPane.showMessageDialog(
                    this.getParent(),
                    "Not enough players!",
                    "ERROR",
                    JOptionPane.WARNING_MESSAGE
            );
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this); // Draws the image of onto the JPanel
    }
}
