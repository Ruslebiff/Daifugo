package client;

import server.Server;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.*;


public class Table extends JPanel {

    private BufferedImage image;
    private final PlayersInformation playersInformation;
    private CardsOnTable cardsOnTable;
    private GameStateTracker stateTracker;
    private Logger LOGGER;


    private Void updateGUI() {
        playersInformation.indicateTurn();
        cardsOnTable.updateCardsOnTable();
        LOGGER.info("Gui got updated");
        return null;
    }

    public Table(int f_width, int f_height, GameStateTracker sT, GameLobby gL) {

        LOGGER = GameLobby.LOGGER;
        this.stateTracker = sT;

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
        cardsOnTable = new CardsOnTable(cardsOnTableWidth, cardsOnTableHeight, stateTracker);
        cardsOnTable.setBounds(
                (f_width /2) - (cardsOnTableWidth/2),
                (f_height /3) - (cardsOnTableHeight/2),
                   cardsOnTableWidth, cardsOnTableHeight
        );
        add(cardsOnTable);


        stateTracker.registerCallback(this::updateGUI);

        JButton startBtn = new JButton("Start");
        startBtn.setBounds(f_width-150,50, 100,50);
        add(startBtn);
        startBtn.addActionListener(e -> startGame());

        JButton exitButton = new JButton("Exit");
        exitButton.setBounds(f_width-150, 100, 100, 50);
        add(exitButton);
        exitButton.addActionListener(l -> {
            gL.setWaitingCursor(true);
            stateTracker.leaveGame();
            this.setVisible(false);
            gL.showLobby(true);
            gL.startHeartbeat();
            gL.setWaitingCursor(false);
        });

        gL.setWaitingCursor(false);
    }

    public void startGame() {
        stateTracker.startGame();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this); // Draws the image of onto the JPanel
    }
}
