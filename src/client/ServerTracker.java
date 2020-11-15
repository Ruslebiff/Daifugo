package client;

import client.networking.ClientConnection;

import java.util.ArrayList;

public class ServerTracker implements GameStateTracker {

    private final ClientConnection connection;

    public ServerTracker(ClientConnection connection) {
        this.connection = connection;
    }
    


    // TODO: go over overrides when syncing with new changes from table_class branch

    @Override
    public String getActivePlayerID() {
        return null;
    }

    @Override
    public ArrayList<Card> dealPlayerHand(String token) {
        return null;
    }

    @Override
    public Boolean checkIfPlayable() {
        return null;
    }

    @Override
    public int getLastPlayedType() {
        return 0;
    }

    @Override
    public void relinquishTurn() {

    }

    @Override
    public void playCards(ArrayList<Card> playedCards) {

    }
}
