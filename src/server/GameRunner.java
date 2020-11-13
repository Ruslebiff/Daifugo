package server;

import protocol.*;
import server.exceptions.GameDisconnect;
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


    private void sendHeartbeatResponse() throws IOException {
        //TODO: check if game state is updated
        long receiveTime = Instant.now().toEpochMilli();
        out.writeObject(
                new HeartbeatMessage(receiveTime)
        );
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

        while (true) {
            try {

                Message request;
                try {
                    request = (Message) in.readObject(); // EOF
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
                    case HEARTBEAT -> sendHeartbeatResponse();
                    case DISCONNECT -> throw new GameDisconnect();
                    default -> out.writeObject(new ErrorMessage("Invalid game request"));
                }


            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                break;
            } catch (GameDisconnect ignored) {
                LOGGER.info(logPrefix + "disconnected during game");
                out.writeObject(new Message(OK));
                throw new GameDisconnect();
            }
        }
    }
}
