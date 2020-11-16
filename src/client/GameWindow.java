package client;

import client.networking.ClientConnection;
import common.GameState;
import protocol.GameStateResponse;
import protocol.Message;
import protocol.MessageType;
import protocol.NewGameMessage;
import server.exceptions.UserSessionError;

import javax.swing.*;
import java.io.IOException;

// The window where the game is
public class GameWindow extends JFrame {
    private final int FRAME_HEIGHT = 1000;
    private final int FRAME_WIDTH = 1000;

    public GameWindow(GameStateTracker stateTracker) throws UserSessionError, IOException, ClassNotFoundException {
        setSize(FRAME_WIDTH,FRAME_HEIGHT);
        setLayout(null);
        setLocationRelativeTo(null);
        setTitle("gameName");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


        Table table = new Table(FRAME_WIDTH, FRAME_HEIGHT, stateTracker);
        table.setBounds(0,0, getWidth(), getHeight());
        add(table);

        setVisible(true);
    }
}
