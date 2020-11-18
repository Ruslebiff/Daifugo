package client;

import server.exceptions.UserSessionError;

import java.io.IOException;

public class ClientMain {
    public static void main(String[] args) throws ClassNotFoundException, IOException, UserSessionError {
        GameLobby lobby = new GameLobby();
    }

}
