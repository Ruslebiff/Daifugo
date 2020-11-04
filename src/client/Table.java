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
    private ArrayList<Card> cards;

    public Table() {
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
        ArrayList<Card> p1 = new ArrayList<>();
        ArrayList<Card> p2 = new ArrayList<>();

        for (int i = 0; i < cards.size(); i++) {
            if(i < 25)
                p1.add(cards.get(i));
            else
                p2.add(cards.get(i));
        }

        /**
         * REMOVE LATER
         */
        Player[] players = new Player[1];
        players[0] = new Player("Mohammed Lee", 0, "President", p1);
        players[0].setBounds(50,700,880,150);
        add(players[0]);
//        players[1] = new Player("John Doe", 1, "Bum", p2);

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
}
