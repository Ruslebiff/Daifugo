package client;

import client.networking.ClientConnection;
import common.PlayerData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ServerTracker implements GameStateTracker {

    private final ClientConnection connection;
    private Callable<Void> guiCallback;

    public void registerCallback(Callable<Void> func) {
        guiCallback = func;
    }



    public ServerTracker(ClientConnection connection) {
        this.connection = connection;
    }


    @Override
    public boolean isMyTurn() {
        return false;
    }

    @Override
    public List<PlayerData> getPlayerList() {
        return null;
    }

    @Override
    public int getActivePlayerIndex() {
        return 0;
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
