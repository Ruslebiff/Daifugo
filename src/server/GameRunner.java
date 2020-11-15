package server;

import protocol.*;
import server.exceptions.GameDisconnect;
import server.exceptions.LeftGame;
import server.exceptions.UserSessionError;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.logging.*;

import static protocol.MessageType.*;

/**
 * Module for handling all server requests while the client is participating
 * in a game.
 */
public class GameRunner {
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final Game game;
    private final UserSession userSession;
    private final String logPrefix;
    private final Logger LOGGER;
    private boolean running;

    public GameRunner(
            UserSession userSession,
            ObjectInputStream in,
            ObjectOutputStream out
    ) {
        this.userSession = userSession;
        this.in = in;
        this.out = out;
        this.game = userSession.getGame();
        this.logPrefix = userSession.getNick() + ": ";
        LOGGER = Logger.getLogger(GameRunner.class.getName());
        LOGGER.addHandler(Server.CONSOLE_HANDLER);
        //LOGGER.addHandler(Server.FILE_HANDLER);
        LOGGER.setLevel(Level.FINE);

        LOGGER.info(
                logPrefix + "joined game: " + game.getTitle()
                        + " (" + game.getID() + ")"
        );
    }

    private void sendHeartbeatResponse(HeartbeatMessage request) throws IOException {

        // if state hasn't been updated, simply heartbeat back
        PlayerObject po = game.getPlayers().get(userSession.getID());
        if (!po.isStateUpdated()) {
            long receiveTime = Instant.now().toEpochMilli();
            out.writeObject(
                    new HeartbeatMessage(receiveTime)
            );

            po.getGameData().setLatency(receiveTime - request.getTime());
            //game.registerGameStateChange();     //TODO: maybe overkill, needs live testing
            return;
        }

        // leave game if cancelled, and inform client
        if (game.isCancelled()) {
            game.leaveGame(userSession.getID());
            out.writeObject(new CancelledGameError());
            running = false;
            return;
        }



        // returning game state response
        try {
            out.writeObject(new GameStateResponse(game, userSession));
        } catch (UserSessionError userSessionError) {
            out.writeObject(new ErrorMessage(userSessionError.getMessage()));
        }


    }

    private boolean userNotOwner() throws IOException {
        try {
            if (!game.getOwnerNick().equals(userSession.getNick())) {
                out.writeObject(new ErrorMessage("Not allowed"));
                return true;
            }
        } catch (UserSessionError userSessionError) {
            out.writeObject(new ErrorMessage(userSessionError.getMessage()));
            return true;
        }

        return false;
    }

    private void validateCancellation() throws IOException, LeftGame {

        if (userNotOwner())
            return;

        game.cancelGame();
        throw new LeftGame();
    }

    private void validateGameStart() throws IOException {
        if (userNotOwner())
            return;

        game.start();

    }

    private void playerDisconnect() throws GameDisconnect {
        userSession.leaveCurrentGame();
        throw new GameDisconnect();
    }

    public void run() throws GameDisconnect, IOException {

        LOGGER.info("Entered game mode");
        try {
            out.writeObject(new GameStateResponse(
                    game, userSession
            ));
        } catch (UserSessionError | IOException userSessionError) {
            out.writeObject(new ErrorMessage(userSessionError.toString()));
        }
        running = true;
        while (running) {
            try {

                Message request;
                try {
                    request = (Message) in.readObject();
                } catch (SocketTimeoutException e) {
                    LOGGER.info("socket exception, breaking runloop");
                    break;
                } catch (EOFException e) {
                    LOGGER.warning(
                            "Got EOF while reading game request. Maybe you forgot to call disconnect()?"
                    );
                    throw new GameDisconnect();
                }

                LOGGER.fine("past first try-catch");

                switch (request.getMessageType()) {
/*                            case RECONNECT -> { //TODO: timeout problem
                            if (!handleReconnection(request))
                                break runLoop;
                        }*/
                    case START_GAME -> validateGameStart();
                    case LEAVE_GAME -> userSession.leaveCurrentGame();
                    case CANCEL_GAME -> validateCancellation();
                    case HEARTBEAT -> sendHeartbeatResponse((HeartbeatMessage) request);
                    case DISCONNECT -> playerDisconnect();
                    default -> out.writeObject(new ErrorMessage("Invalid game request"));
                }


            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                break;
            } catch (GameDisconnect ignored) {
                LOGGER.info(logPrefix + "disconnected during game");
                out.writeObject(new Message(OK));
                throw new GameDisconnect();
            } catch (LeftGame ignored) {
                LOGGER.info(logPrefix + "left game: " + game.getID().toString());
                break;
            }
        }
    }
}
