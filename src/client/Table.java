package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Table extends JPanel {

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

        // The deck of cards, H(earts), S(pades), C(lubs), D(iamond)
        ArrayList<Card> cardList = new ArrayList<Card>(52);
        char[] suits = {'H', 'S', 'C', 'D'};

        for (int suit = 0; suit < 4; suit++) {      // For each suit, create 13 cards
            for (int number = 2; number < 14; number++) {
                cardList.add(new Card(number, suits[suit]));
            }
        }

        Collections.shuffle(cardList);          // Shuffle the cards
        cards = new ArrayList<Card>(cardList); // The deck of cards that the players will use

        Player[] players = new Player[1];
        players[0] = new Player("Mohammed Lee", 0, "President", cards);
//        players[1] = new Player("John Doe", 2, "Bum", cards);

        PlayersInformation playersInformation = new PlayersInformation(players);
        playersInformation.setBounds(50,50, pInfoWidth, pInfoHeight);
        add(playersInformation);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this); // Draws the image of onto the JPanel
    }
}
