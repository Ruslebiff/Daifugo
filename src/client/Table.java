package client;

import common.PlayerData;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Table extends JPanel {

    private BufferedImage image;
    private final int pInfoWidth = 200;
    private final int pInfoHeight = 100;
    private int TABLE_HEIGHT;
    private int TABLE_WIDTH;
    private final int deckSize = 52;
    private PlayersInformation playersInformation;
    private CardsOnTable cardsOnTable;
    private GameStateTracker stateTracker;
    private JButton startBtn;

    private Void updateGUI() {
        playersInformation.indicateTurn();
        cardsOnTable.updateCardsOnTable();
        return null;
    }

    public Table(int f_width, int f_height, GameStateTracker sT) {
        this.TABLE_WIDTH = f_width;
        this.TABLE_HEIGHT = f_height;
        this.stateTracker = sT; // TODO: Bytt ut med ordentlig tracker
        try {
            image = ImageIO.read(
                    ClientMain.class.getResourceAsStream("/green_fabric.jpg"));       // Read the image
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        setLayout(null);

        // TODO: REMOVE LATER
        Player[] players = new Player[1];
        players[0] = new Player(
                "Mohammed Lee",
                "0",
                stateTracker.getHand("temp"),
                TABLE_WIDTH/2,
                stateTracker
        );

        players[0].setBounds(
                (TABLE_WIDTH/2) - ((TABLE_WIDTH/2)/2) - 25,
                (TABLE_HEIGHT/2) + 100,
                TABLE_WIDTH/2,
                (TABLE_HEIGHT/8) + 100
        );
        add(players[0]);

        playersInformation = new PlayersInformation(players, stateTracker);
        playersInformation.setBounds(50,50, pInfoWidth, pInfoHeight);
        add(playersInformation);

        int cardsOnTableWidth = 300, cardsOnTableHeight = 200;
        cardsOnTable = new CardsOnTable(cardsOnTableWidth, cardsOnTableHeight, stateTracker);
        cardsOnTable.setBounds(
                (TABLE_WIDTH/2) - (cardsOnTableWidth/2),
                (TABLE_HEIGHT/3) - (cardsOnTableHeight/2),
                   cardsOnTableWidth, cardsOnTableHeight
        );
        add(cardsOnTable);


        stateTracker.registerCallback(this::updateGUI);

        startBtn = new JButton("Start");
        startBtn.setBounds(50,200, 100,50);
        add(startBtn);

        startBtn.addActionListener(e -> startGame());
    }

    public void startGame() {
//        stateTracker
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this); // Draws the image of onto the JPanel
    }
}
