package client;

import client.networking.ClientConnection;
import common.GameState;
import common.PlayerData;
import common.Role;
import common.Trick;
import protocol.*;

import java.io.IOException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static client.GameLobby.LOGGER;

public class ServerTracker implements GameStateTracker {

    private final ClientConnection connection;
    private Callable<Void> guiCallback;
    private GameState state;
    private final long heartbeatInterval = 100;

    private final HeartbeatThread backgroundThread;

    private boolean cancelled;
    private Callable<Void> connectionLost;



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
                try {
                    synchronized (ServerTracker.this) {
                        LOGGER.fine("Sending heartbeat");
                        long timestamp = Instant.now().toEpochMilli();
                        response = connection.sendMessage(
                                new HeartbeatMessage(timestamp)
                        );

                        if (response.isError()) {
                            LOGGER.warning(
                                    "Received error: " + response.getErrorMessage()
                            );
                            LOGGER.warning("Error type: " + response.getMessageType());
                            if (response.getMessageType() == MessageType.CANCEL_GAME_ERROR) {
                                cancelled = true;
                                guiCallback.call();
                            }

                            break;

                        }

                        if (response.getMessageType() == MessageType.GAME_STATE) {
                            GameStateResponse tmp = (GameStateResponse) response;
                            state = tmp.getState();
                            guiCallback.call();
                        }


                }

                Thread.sleep(heartbeatInterval);
                } catch (Exception e) {
                    LOGGER.warning("Heartbeat resulted in exception: " + Arrays.toString(e.getStackTrace()));
                    try {
                        e.printStackTrace();
                        connectionLost.call();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    break;
                }
            }

        }

    }

    public ServerTracker(ClientConnection connection, GameState state) {
        this.connection = connection;
        this.state = state;
        backgroundThread = new HeartbeatThread();
        cancelled = false;
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
        synchronized (this) {
            return state.getCurrentPlayer();
        }
    }


    @Override
    public boolean isCancelled() {
        synchronized (this) {
            return cancelled;
        }
    }

    @Override
    public void cancelGame() {
        try {
            synchronized (this) {
                Message response = connection.sendMessage(new Message(MessageType.CANCEL_GAME));
                if (response.isError()) {
                    LOGGER.warning("Failed to cancel game: " + response.getErrorMessage());
                    return;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warning("Exception during cancellation of game: " + e.getMessage());
            return;
        }

        try {
            stopHeartbeatThread();
        } catch (InterruptedException e) {
            LOGGER.warning("failed to stop heartbeat on game cancel: " + e.getMessage());
        }

    }

    @Override
    public void leaveGame() {
        LOGGER.info("Entered leave game");
        synchronized (this) {
            try {
                Message response = connection.sendMessage(MessageType.LEAVE_GAME);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            stopHeartbeatThread();
        } catch (InterruptedException e) {
            LOGGER.warning("failed to stop heartbeat on game leave: " + e.getMessage());
        }
    }


    @Override
    public List<Card> getHand() {
        synchronized (this) {
            try {
                return state.getHand()
                        .stream().map(card -> new Card(card.getNumber(), card.getSuit()))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                LOGGER.warning(e.getStackTrace().toString());
                return new ArrayList<>();
            }
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
                if (response.isError()) {
                    LOGGER.warning("Tracker received error playing cards: " + response.getErrorMessage());
                    return false;
                }

            } catch (Exception e) {
                LOGGER.warning(e.getMessage());
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
                if(response.isError()) {
                    LOGGER.warning("Error giving cards: " + response.getErrorMessage());
                    return false;
                }

            } catch (Exception e) {
                LOGGER.warning("Exception giving cards: " + e.toString());
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
        LOGGER.info("Entered servertrackers startgame");
        synchronized (this) {
            LOGGER.info("Entered synchronized block");
            Message response = null;
            try {
                LOGGER.info("Sending start game message..");
                response = connection.sendMessage(MessageType.START_GAME);
                if(response.isError()){
                    LOGGER.warning("Sending start message failed: " + response.getErrorMessage());
                    return false;
                } else {
                    LOGGER.info("Start sent successfully");
                }

            } catch (Exception e) {
                LOGGER.warning("Starting game resulted in exception: " + e.getMessage());
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
                response = connection.sendMessage(MessageType.STOP_GAME);
                if(response.isError())
                    return false;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    @Override
    public boolean isStarted() {
        synchronized (this) {
            return state.isStarted();
        }
    }

    @Override
    public int getCardsInTrick() {
        synchronized (this) {
            return state.getCardsInTrick();
        }
    }

    @Override
    public int getRoundNo() {
        synchronized (this) {
            return state.getRoundNo();
        }
    }

    @Override
    public boolean isTradingPhase() {
        synchronized (this) {
            return state.isTradingPhase();
        }
    }

    @Override
    public boolean iHaveToTrade() {
        synchronized (this) {
            return state.haveToTrade();
        }
    }

    @Override
    public int getMyRoleNumber() {
        int noOfPlayers = state.getPlayers().size();
        int roleNo;
        synchronized (this) {
            switch (state.getRole()) {
                case BUM -> roleNo = (noOfPlayers == 3) ? -1 : -2;
                case PRESIDENT -> roleNo = (noOfPlayers == 3) ? 1 : 2;
                case VICE_BUM -> roleNo = -1;
                case VICE_PRESIDENT -> roleNo = 1;
                default -> roleNo = 0;
            }
        }
        return roleNo;
    }

    @Override
    public void registerConnectionLostCallback(Callable<Void> func) {
        connectionLost = func;
    }

    @Override
    public Role getRole() {
        synchronized (this) {
            return state.getRole();
        }
    }

    public int getCardsInPlay() {
        synchronized (this) {
            return state.getCardsOnTable();
        }
    }

    @Override
    public boolean isNewTrick() {
        synchronized (this) {
            return state.getLastTrick() != Trick.NONE;
        }
    }

    @Override
    public Trick getLastTrick() {
        synchronized (this) {
            return state.getLastTrick();
        }
    }


    @Override
    public int getGoneOutNumber() {
        synchronized (this) {
            return state.getOutCount();
        }
    }

    @Override
    public String getGameTitle() {
        synchronized (this) {
            return state.getGameTitle();
        }
    }

    @Override
    public int getNumberOfFaceDownCards() {
        synchronized (this) {
            return state.getFaceDownCards();
        }
    }
}
