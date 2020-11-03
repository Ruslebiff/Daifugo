package client;

import javax.swing.*;

// The window where the game is
public class GameWindow extends JFrame {
    private final int HEIGHT = 1000;
    private final int WIDTH = 1000;



    public GameWindow(String gameName) {
        setSize(WIDTH,HEIGHT);
        setLayout(null);
        setTitle(gameName);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Table table = new Table();
        table.setBounds(0,0, getWidth(), getHeight());
        add(table);

        setVisible(true);
    }
}
