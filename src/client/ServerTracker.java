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

public class ServerTracker implements GameStateTracker {

    private final ClientConnection connection;
    private Callable<Void> guiCallback;
    private GameState state;
    private final long heartbeatInterval = 300;

    private boolean runHeartbeat;
    private HeartbeatThread backgroundThread;

    private ArrayList<Card> deck = new ArrayList<>();
    private ArrayList<Card> p1_cards = new ArrayList<>();   // TODO: REMOVE
    private ArrayList<Card> lastPlayedCards = new ArrayList<>();    // array list of the cards played
    private ArrayList<Card> allCardsInRound = new ArrayList<>();    // array list of the cards played
    private int playedType = 0;     // integer indicating if the cards played are singles, doubles or triples

    // TODO: temporary list of cards, remove later
    ArrayList<Card> tmp = new ArrayList<>();



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
    public List<Card> getHand(String token) {
        synchronized (this) {
            return state.getHand()
                    .stream().map(card -> new Card(card.getNumber(), card.getSuit()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public int getLastPlayedType() {
        return 0;
    }

    @Override
    public void passTurn() {
        synchronized (this) {
            Message response = null;
            try {
                response = connection.sendMessage(new PassTurn());
                if(response.isError())
                    System.out.println(response.getErrorMessage());

                GameStateResponse tmp = (GameStateResponse) response;
                state = tmp.getState();
                guiCallback.call();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setNextTurn() {
        synchronized (this) {

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
    public boolean isNewTrick() {
        return false;
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
    public void resetRound() {
        synchronized (this) {
            allCardsInRound.removeAll(allCardsInRound);
            lastPlayedCards.removeAll(lastPlayedCards);
            playedType = 0;
        }
    }

    @Override
    public int getNumberOfFaceDownCards() {
        synchronized (this) {
            return state.getFaceDownCards();
        }
    }
}
