package client;

import client.networking.ClientConnection;

import java.util.ArrayList;

public class ServerTracker implements GameStateTracker {

    private final ClientConnection connection;

    public ServerTracker(ClientConnection connection) {
        this.connection = connection;
    }
    



    @Override
    public String getActivePlayerID() {
        return null;
    }

    @Override
    public ArrayList<Card> getHand(String token) {
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
    public void setNextTurn() {

    }

    @Override
    public boolean playCards(ArrayList<Card> playedCards) {
        return false;
    }

    @Override
    public boolean giveCards(ArrayList<Card> cards, int role) {
        return false;
    }

    @Override
    public ArrayList<Card> getLastPlayedCards() {
        return null;
    }
}
