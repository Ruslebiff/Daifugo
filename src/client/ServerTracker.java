package client;

import client.networking.ClientConnection;
import common.GameState;
import common.PlayerData;
import protocol.*;

import java.io.IOException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static client.GameLobby.LOGGER;

public class ServerTracker implements GameStateTracker {

    private final ClientConnection connection;
    private Callable<Void> guiCallback;
    private GameState state;
    private final long heartbeatInterval = 300;

    private HeartbeatThread backgroundThread;

    private ArrayList<Card> lastPlayedCards = new ArrayList<>();    // array list of the cards played
    private ArrayList<Card> allCardsInRound = new ArrayList<>();    // array list of the cards played



    //TODO: trading cards from round 2

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
                        LOGGER.fine("Sending heartbeat");
                        long timestamp = Instant.now().toEpochMilli();
                        response = connection.sendMessage(
                                new HeartbeatMessage(timestamp)
                        );

                        if (response.isError()) {
                            LOGGER.warning(
                                    "Received error: " + response.getErrorMessage()
                            );
                            break;
                        }

                        if (response.getMessageType() == MessageType.GAME_STATE) {
                            GameStateResponse tmp = (GameStateResponse) response;
                            state = tmp.getState();
                            guiCallback.call();
                        }

                        Thread.sleep(heartbeatInterval);

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
        backgroundThread = new HeartbeatThread();


    }

    public void stopHeartbeatThread() throws InterruptedException {
        backgroundThread.sendStopSignal();
        backgroundThread.join();
    }

    @Override
    public void registerCallback(Callable<Void> func) {
        guiCallback = func;
        backgroundThread.start();
    }

    @Override
    public boolean isOwner() {
        synchronized (this) {
           return state.iAmOwner();
        }
    }

    @Override
    public boolean isMyTurn() {
        synchronized (this) {
            return state.isMyTurn();
        }
    }

    @Override
    public List<PlayerData> getPlayerList() {
        synchronized (this) {
            return state.getPlayers();
        }
    }

    @Override
    public int getActivePlayerIndex() {
        return 0;
    }

    @Override
    public int getMyPlayerId() {
        synchronized (this) {
            return 0;
        }
    }

    @Override
    public void leaveGame() {
        try {
            Message response = connection.sendMessage(MessageType.LEAVE_GAME);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            stopHeartbeatThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public List<Card> getHand() {
        synchronized (this) {
            return state.getHand()
                    .stream().map(card -> new Card(card.getNumber(), card.getSuit()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void passTurn() {
        synchronized (this) {
            Message response = null;
            try {
                response = connection.sendMessage(new PassTurn());
                if(response.isError())
                    LOGGER.warning(response.getErrorMessage());

                GameStateResponse tmp = (GameStateResponse) response;
                state = tmp.getState();
                guiCallback.call();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean playCards(List<Card> playedCards) {
        synchronized (this) {
            Message response = null;
            try {
                response = connection.sendMessage(new PlayCardsRequest(playedCards));
                if (response.isError())
                    return false;

                GameStateResponse tmp = (GameStateResponse) response;
                state = tmp.getState();
                guiCallback.call();
            } catch (Exception e) {
                return false;
            }
            return true;
        }
    }

    @Override
    public boolean giveCards(List<Card> cards) {
        synchronized (this) {
            Message response = null;
            try {
                response = connection.sendMessage(new GiveCardsRequest(cards));
                if(response.isError())
                    return false;

                GameStateResponse tmp = (GameStateResponse) response;
                state = tmp.getState();
                guiCallback.call();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    @Override
    public List<Card> getCardsOnTable() {
        synchronized (this) {
            return state.getTopCards()
                    .stream().map(card -> new Card(card.getNumber(), card.getSuit()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public boolean startGame() {
        synchronized (this) {
            Message response = null;
            try {
                response = connection.sendMessage(MessageType.START_GAME);
                if(response.isError())
                    return false;

                GameStateResponse tmp = (GameStateResponse) response;
                state = tmp.getState();
                guiCallback.call();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    @Override
    public boolean stopGame() {
        synchronized (this) {
            Message response = null;
            try {
                response = connection.sendMessage(MessageType.CANCEL_GAME);
                if(response.isError())
                    return false;

                GameStateResponse tmp = (GameStateResponse) response;
                state = tmp.getState();
                guiCallback.call();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    @Override
    public boolean hasStarted() {
        synchronized (this) {
            return state.isStarted();
        }
    }


    @Override
    public int getNumberOfFaceDownCards() {
        synchronized (this) {
            return state.getFaceDownCards();
        }
    }
}
