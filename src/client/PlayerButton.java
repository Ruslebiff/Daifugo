package client;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class PlayerButton extends JButton {
    private int btnWidth;
    private int btnHeight;
    public PlayerButton(int x, int y, int w, int h, String txt) {
        setBounds(x,y,btnWidth = w, btnHeight = h);
        setText(txt);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);       // Rounds the corner of the cards
        RoundRectangle2D r = new RoundRectangle2D.Float(0, 0, btnWidth, btnHeight, 10, 10);
        g.setClip(r);
    }
}
