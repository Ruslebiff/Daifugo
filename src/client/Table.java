package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Table extends JPanel {

    private BufferedImage image;
    private final String filePath;
    private final int pInfoWidth = 200;
    private final int pInfoHeight = 100;

    public Table() {
        this.filePath = "./resources/green_fabric.jpg"; // Filepath
        try {
            image = ImageIO.read(new File(filePath));       // Read the image
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        setLayout(null);

        Player[] players = new Player[2];
        players[0] = new Player("Mohammed Lee", 0, "President");
        players[1] = new Player("John Doe", 1, "Bum");

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
