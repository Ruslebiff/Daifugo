package client;

import server.exceptions.UserSessionError;

import java.io.IOException;

public class ClientMain {
    public static void main(String[] args) throws ClassNotFoundException, IOException, UserSessionError {
        System.out.println("Hello, I'm the client!");


        GameLobby lobby = new GameLobby();
//        GameWindow gm = new GameWindow();
    }

}
