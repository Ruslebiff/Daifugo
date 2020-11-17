package server;

import common.*;
import protocol.*;
import server.exceptions.*;

import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.*;
import java.util.logging.*;

import static protocol.MessageType.*;


public class Server {
    public static final ConsoleHandler CONSOLE_HANDLER = new ConsoleHandler();
    public static FileHandler FILE_HANDLER = null;

    static {
        try {
            FILE_HANDLER = new FileHandler(
                    "%h/daifugo-server.log",
                    true
            );
            System.setProperty(
                    "java.util.logging.SimpleFormatter.format",
                    "[%4$s] (%1$ta %1$tF %1$tT %1$tZ) - %3$s:  %5$s %n"
            );
            FILE_HANDLER.setFormatter(new SimpleFormatter());
            CONSOLE_HANDLER.setFormatter(new SimpleFormatter());

            // Setting default log level to finest, overridden by local LOGGER object
            FILE_HANDLER.setLevel(Level.ALL);
            CONSOLE_HANDLER.setLevel(Level.ALL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ServerSocket serverSocket;
    public static final Logger SERVER_LOGGER = Logger.getLogger(
            Server.class.getName()
    );

    public Server() {
        SERVER_LOGGER.setLevel(Level.INFO);
        SERVER_LOGGER.addHandler(CONSOLE_HANDLER);
        SERVER_LOGGER.addHandler(FILE_HANDLER);
    }

    public void start(int port) throws IOException {
        SERVER_LOGGER.info("Daifugo server starting on port " + port + "...");
        serverSocket = new ServerSocket(port);
        while (true)
            new Server.ClientHandler(serverSocket.accept()).start();
    }

    public void stop() throws IOException {
        SERVER_LOGGER.info("Stopping server...");
        serverSocket.close();

        // ensures that subsequent server restarts during testing
        // doesn't continue counting from last user number
        UserSession._reset();
        Game._reset();
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start(Protocol.PORT);
    }

    private static class ClientHandler extends Thread {
        private static Logger LOGGER;
        private final Socket clientSocket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private UserSession currentSession = null;

        public ClientHandler(Socket socket) throws SocketException {
            clientSocket = socket;
            clientSocket.setSoTimeout(Protocol.SOCKET_TIMEOUT);
            LOGGER = SERVER_LOGGER;
            LOGGER.info("New connection from: " + clientSocket.getInetAddress().toString());
        }

        private void sendInvalidRequest() throws IOException {
            sendError("Invalid request");
        }

        private void sendError(String msg) throws IOException {
            out.writeObject(
                    new ErrorMessage(msg)
            );
        }

        private void sendHeartbeatResponse() throws IOException {
            long receiveTime = Instant.now().toEpochMilli();
            out.writeObject(
                    new HeartbeatMessage(receiveTime)
            );
        }

        //TODO: timeout problem
/*        private boolean handleReconnection(Message request) throws IOException {
            if (currentSession != null) {
                sendInvalidRequest();
                return false;
            }

            LOGGER.info("handling reconnect");
            try {
                ReconnectMessage tmp = (ReconnectMessage) request;
                currentSession = UserSession.retrieveSessionFromToken(
                        tmp.getToken()
                );
            } catch (UserSessionError e) {
                sendError(e.toString());
            }
            return true;
        }*/

        private void createNewSession() throws IOException {
            if (currentSession != null) {
                out.writeObject(new ErrorMessage("Already connected"));
                return;
            }

            currentSession = new UserSession();
            out.writeObject(
                    new IdentityResponse(
                        currentSession.getToken(),
                        currentSession.getNick()
                    )
            );

            LOGGER = Logger.getLogger(
                    "User: " + currentSession.getToken() + clientSocket.getInetAddress().toString()
            );
            LOGGER.addHandler(CONSOLE_HANDLER);
            LOGGER.setLevel(Level.INFO);
            // TODO: add file logging:
            //LOGGER.addHandler(FILE_HANDLER);
        }

        private void updateNick(UpdateNickMessage request) throws IOException {
            LOGGER.info("Received request to change nick form " + currentSession.getNick());
            try {
                String tmp = currentSession.getNick();
                currentSession.setNick(request.getNick());
                out.writeObject(
                        new IdentityResponse(
                                request.getToken(),
                                currentSession.getNick()
                        )
                );
                LOGGER.info("Successfully changed nick from " + tmp + " to " + currentSession.getNick());
            }
            catch (UserSessionError e) {
                out.writeObject(
                        new ErrorMessage(e.toString())
                );
            }
        }

        private void sendGameList() throws IOException {
            List<GameListing> list;

            try {
                list = Game.getGameList();
            } catch (UserSessionError | GameException e) {
                out.writeObject(new ErrorMessage(e.toString()));
                return;
            }

            out.writeObject(
                    new GameListResponse(list)
            );
        }

        private void createNewGame(NewGameMessage request) throws IOException, GameDisconnect {
            LOGGER.info("Creating new game");
            Game game;
            try {
                game = new Game(
                        currentSession.getID(),
                        request.getTitle(),
                        request.getPassword()
                );
                LOGGER.info(currentSession.getNick() + " created game: " + game.getTitle() + " " + game.getID().toString());
            } catch (UserSessionError | GameException e) {
                out.writeObject(new ErrorMessage(e.toString()));
                return;
            }
            runGameMode();
        }

        private void joinExistingGame(JoinGameRequest request) throws IOException, GameDisconnect {
            Game game = Game.getGameByID(UUID.fromString(request.getGameID()));
            try {
                game.joinGame(currentSession, request.getPassword());

            } catch (GameException e) {
                out.writeObject(new ErrorMessage(e.getMessage()));
                return;
            } catch (WrongPassword ignored) {
                out.writeObject(new PasswordError());
                return;
            }

            runGameMode();
        }

        /**
         * Runs a separate game loop, thus entering a new mode of responding
         * to client messages
         */
        private void runGameMode() throws IOException, GameDisconnect {
            new GameRunner(currentSession, in, out).run();
        }

        public void run() {

            // setting up object channels
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }


            // Starting communication loop
            runLoop:
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
                                "Got EOF while reading request from client. Maybe you forgot to call disconnect()?"
                                //  maybe this happens because DISCONNECT not sent?
                                // yup
                                // no, it happens because unclean socket termination
                        );
                        //LOGGER.warning("Disconnecting from client");
                        //request = new Message(DISCONNECT);
                        break;
                    }

                    LOGGER.fine("past first try-catch");

                    if (currentSession == null
                            && request.getMessageType() != CONNECT
                            //&& request.getMessageType() != RECONNECT
                            && request.getMessageType() != DISCONNECT
                    ) {
                        sendInvalidRequest();

                    } else {
                        if (currentSession != null)
                            LOGGER.fine("Awaiting request from: " + currentSession.getToken() + "|" + currentSession.getNick());
                        switch (request.getMessageType()) {
                            case CONNECT -> createNewSession();
/*                            case RECONNECT -> { //TODO: timeout problem
                                if (!handleReconnection(request))
                                    break runLoop;
                            }*/
                            case HEARTBEAT -> sendHeartbeatResponse();
                            case UPDATE_NICK -> updateNick((UpdateNickMessage) request);
                            case GET_GAME_LIST -> sendGameList();
                            case NEW_GAME -> createNewGame((NewGameMessage) request);
                            case JOIN_GAME -> joinExistingGame((JoinGameRequest) request);
                            case DISCONNECT -> {
                                if (currentSession != null) {
                                    currentSession.endSession();
                                }
                                out.writeObject(new Message(MessageType.OK));
                                break runLoop;
                            }
                            default -> sendError("Non-implemented request type");
                        }
                    }


                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    break;
                } catch (GameDisconnect ignored) {
                    currentSession.endSession();
                    break;
                }
            }


            // Done with run loop, closing connection.
            try {
                LOGGER.info("Closing server connection...");
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
