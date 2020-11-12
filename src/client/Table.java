package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Table extends JPanel implements GameStateTracker {

    private BufferedImage image;
    private final String filePath;
    private final int pInfoWidth = 200;
    private final int pInfoHeight = 100;
    private int TABLE_HEIGHT;
    private int TABLE_WIDTH;
    private ArrayList<Card> cards;

    public Table(int f_width, int f_height) {
        this.TABLE_WIDTH = f_width;
        this.TABLE_HEIGHT = f_height;
        this.filePath = "./resources/green_fabric.jpg"; // Filepath
        try {
            image = ImageIO.read(new File(filePath));       // Read the image
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        setLayout(null);

        cards = getDeck();  // Get the shuffled deck from the interface

        /**
         * REMOVE LATER
         */
        ArrayList<Card> p1_cards = new ArrayList<>();;
        for (int i = 0; i < cards.size()/3; i++) {
            p1_cards.add(cards.get(i));
        }

        /**
         * REMOVE LATER
         */
        Player[] players = new Player[1];
        players[0] = new Player("Mohammed Lee", 0, p1_cards, TABLE_WIDTH/2);
        players[0].setBounds((TABLE_WIDTH/2) - ((TABLE_WIDTH/2)/2) - 25,(TABLE_HEIGHT/2) + 100,TABLE_WIDTH/2,(TABLE_HEIGHT/8) + 100);
        add(players[0]);


        PlayersInformation playersInformation = new PlayersInformation(players);
        playersInformation.setBounds(50,50, pInfoWidth, pInfoHeight);
        add(playersInformation);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this); // Draws the image of onto the JPanel
    }

    @Override
    public String getActivePlayerID() {
        return null;
    }

    @Override
    public ArrayList<Card> dealPlayerHand(String token) {
        return null;
    }

    @Override
    public Boolean checkIfPlayable() {
        return null;
    }

    @Override
    public int getLastPlayedType() {
        return 0;
    }

    @Override
    public void relinquishTurn() {

    }

}
