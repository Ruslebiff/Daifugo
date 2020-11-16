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

    public GameWindow() throws UserSessionError, IOException, ClassNotFoundException {
        setSize(FRAME_WIDTH,FRAME_HEIGHT);
        setLayout(null);
        setLocationRelativeTo(null);
        setTitle("gameName");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        /** Kun testing for lobby knyttes inn **/
        ClientConnection conn = new ClientConnection("localhost");
        conn.sendMessage(new Message(MessageType.CONNECT));
        GameStateResponse gsr =  (GameStateResponse) conn.sendMessage(
                new NewGameMessage("spillet", null)
        );


        GameStateTracker stateTracker = new ServerTracker(
                conn,
                gsr.getState()
        );

        /*************/

        Table table = new Table(FRAME_WIDTH, FRAME_HEIGHT, stateTracker);
        table.setBounds(0,0, getWidth(), getHeight());
        add(table);

        setVisible(true);
    }
}
