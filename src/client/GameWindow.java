package client;

import javax.swing.*;

// The window where the game is
public class GameWindow extends JFrame {
    private final int FRAME_HEIGHT = 1000;
    private final int FRAME_WIDTH = 1000;

    public GameWindow() {
        setSize(FRAME_WIDTH,FRAME_HEIGHT);
        setLayout(null);
        setLocationRelativeTo(null);
        setTitle("gameName");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        GameStateTracker stateTracker = new DummyTracker();

        Table table = new Table(FRAME_WIDTH, FRAME_HEIGHT, stateTracker);
        table.setBounds(0,0, getWidth(), getHeight());
        add(table);

        setVisible(true);
    }
}
