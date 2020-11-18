package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class FaceDownCard extends JPanel {
    private BufferedImage image;
    private Image scaledImage;
    private int cardWidth = 80;
    private int cardHeight = 120;

    public FaceDownCard(){
        setOpaque(false);
        try {
            image = ImageIO.read(
                    ClientMain.class.getResourceAsStream("./cardimages/Daifugo_cardback_fade_blue_vertical.png")
            );  // Read the image
            scaledImage = image.getScaledInstance(cardWidth,cardHeight, Image.SCALE_SMOOTH);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);       // Rounds the corner of the cards
        RoundRectangle2D r = new RoundRectangle2D.Float(0, 0, cardWidth, cardHeight, 10, 10);
        g.setClip(r);
        g.drawImage(scaledImage, 0, 0, this); // Draws the image of onto the JPanel
    }
}
