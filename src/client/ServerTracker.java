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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class ServerTracker implements GameStateTracker {

    private final ClientConnection connection;
    private Callable<Void> guiCallback;
    private GameState state;
    private final long hearbeatInterval = 300;

    private boolean runHeartbeat;
    private HeartbeatThread backgroundThread;

    private ArrayList<Card> deck = new ArrayList<>();
    private ArrayList<Card> p1_cards = new ArrayList<>();   // TODO: REMOVE
    private ArrayList<Card> lastPlayedCards = new ArrayList<>();    // array list of the cards played
    private ArrayList<Card> allCardsInRound = new ArrayList<>();    // array list of the cards played
    private int playedType = 0;     // integer indicating if the cards played are singles, doubles or triples

    // TODO: temporary list of cards, remove later
    ArrayList<Card> tmp = new ArrayList<>();




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

        /*** REMOVE LATER                           ***/
        Card c1 = new Card(5, 'S');
        Card c2 = new Card(6, 'S');
        tmp.add(c1);
        tmp.add(c2);
        /**********************************************/

        char[] suits = {'H', 'S', 'C', 'D'}; // H(earts), S(pades), C(lubs), D(iamond)
        for (int suit = 0; suit < 4; suit++)        // For each suit, create 13 cards
            for (int number = 2; number < 15; number++)
                deck.add(new Card(number, suits[suit]));    // Add the card to the cardList
        Collections.shuffle(deck);          // Shuffle the cards

        for (int i = 0; i < deck.size()/3; i++) {
            p1_cards.add(deck.get(i));      // TODO: Change later
        }
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
            return false;
        }
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
        return "0";
    } //TODO:

    @Override
    public ArrayList<Card> getHand(String token) {
//        synchronized (this) {
//
//        }

        return p1_cards;
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
        return true;
    }       // TODO:

    @Override
    public boolean playCards(ArrayList<Card> playedCards) {
        synchronized (this) {
            playedType = playedCards.size();
            this.lastPlayedCards.removeAll(lastPlayedCards);
            this.lastPlayedCards.addAll(playedCards);
            this.allCardsInRound.addAll(playedCards);
            return true;
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
        // TODO:
        return tmp;
    }

    @Override
    public ArrayList<Card> getRoundCards() {
        //TODO:
        return tmp;
    }

    @Override
    public void resetRound() {
        synchronized (this) {
            allCardsInRound.removeAll(allCardsInRound);
            lastPlayedCards.removeAll(lastPlayedCards);
            playedType = 0;
        }
    }
}
