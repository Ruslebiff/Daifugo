package client;

import server.exceptions.UserSessionError;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class ClientMain {
    public static void main(String[] args) throws ClassNotFoundException, IOException, UserSessionError {
        GameLobby lobby = new GameLobby();
    }

}
