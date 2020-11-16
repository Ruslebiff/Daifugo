package client;

import client.networking.ClientConnection;
import common.GameState;
import common.PlayerData;
import protocol.GameStateResponse;
import protocol.HeartbeatMessage;
import protocol.Message;
import protocol.MessageType;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ServerTracker implements GameStateTracker {

    private final ClientConnection connection;
    private Callable<Void> guiCallback;
    private GameState state;
    private final long hearbeatInterval = 300;

    private boolean runHeartbeat;
    private HeartbeatThread backgroundThread;



    private class HeartbeatThread extends Thread {

        private boolean running;

        public HeartbeatThread() {
            running = true;
        }

        public void sendStopSignal() {
            running = false;
        }

        public void run() {

            Message response;

            while (running) {
                synchronized (ServerTracker.this) {
                    try {
                        long timestamp = Instant.now().toEpochMilli();
                        response = connection.sendMessage(
                                new HeartbeatMessage(timestamp)
                        );

                        if (response.isError()) {
                            // TODO: change to logging
                            System.out.println(
                                    "Received error: " + response.getErrorMessage()
                            );
                            break;
                        }

                        if (response.getMessageType() == MessageType.GAME_STATE) {
                            GameStateResponse tmp = (GameStateResponse) response;
                            state = tmp.getState();
                            guiCallback.call();
                        }

                        Thread.sleep(hearbeatInterval);

                    } catch (Exception e) {
                        e.printStackTrace();        // TODO: change to logging
                        break;
                    }
                }
            }

        }

    }





    public ServerTracker(ClientConnection connection, GameState state) {
        this.connection = connection;
        this.state = state;
        runHeartbeat = true;
        backgroundThread = new HeartbeatThread();
    }

    public void stopHeartbeatThread() throws InterruptedException {
        runHeartbeat = false;
        backgroundThread.join();
    }

    @Override
    public void registerCallback(Callable<Void> func) {
        guiCallback = func;
        backgroundThread.start();
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
    public boolean getIsMyTurn() {
        return false;
    }

    @Override
    public boolean playCards(ArrayList<Card> playedCards) {
        synchronized (this) {
            return false;
        }
    }

    @Override
    public boolean giveCards(ArrayList<Card> cards, int role) {
        return false;
    }

    @Override
    public boolean isNewTrick() {
        return false;
    }

    @Override
    public ArrayList<Card> getLastPlayedCards() {
        return null;
    }

    @Override
    public ArrayList<Card> getRoundCards() {
        return null;
    }

    @Override
    public void resetRound() {

    }

    @Override
    public void setAllFourSameCards() {

    }
}
