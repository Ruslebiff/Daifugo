package server;

import protocol.*;

import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.logging.*;

import static protocol.MessageType.CONNECT;
import static protocol.MessageType.HEARTBEAT;


public class Server {
    private static final ConsoleHandler CONSOLE_HANDLER = new ConsoleHandler();
    private static FileHandler FILE_HANDLER = null;

    static {
        try {
            FILE_HANDLER = new FileHandler(
                    "%h/daifugo-server.log",
                    true
            );
            System.setProperty(
                    "java.util.logging.SimpleFormatter.format",
                    "[%1$ta %1$tF %1$tT %1$tZ] %4$s: %5$s %n"
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
    private static final Logger SERVER_LOGGER = Logger.getLogger(
            ClientHandler.class.getName()
    );

    public Server() {
        SERVER_LOGGER.setLevel(Level.ALL);
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
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start(Protocol.PORT);
    }

    private static class ClientHandler extends Thread {
        private static final Logger LOGGER = Logger.getLogger(
                ClientHandler.class.getName()
        );
        private final Socket clientSocket;
        private ObjectOutputStream out;
        private ObjectInputStream in;

        public ClientHandler(Socket socket) throws SocketException {
            clientSocket = socket;
            clientSocket.setSoTimeout(Protocol.SOCKET_TIMEOUT);
            LOGGER.addHandler(CONSOLE_HANDLER);
            LOGGER.setLevel(Level.INFO);
            // TODO: add file logging:
            //LOGGER.addHandler(FILE_HANDLER);
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

        // TODO: find out if such a thing is needed now
/*
        private void diagnosticHandler(List<String> request) {
            for (String msg : request) {
                out.println(msg);
            }
        }
*/

        private void createNewSession() throws IOException {
            UserSession session = new UserSession();
            out.writeObject(new IdentityResponse(session));
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
            runloop:
            while (true) {
                try {

                    Message request = (Message) in.readObject();
                    if (request == null)
                        throw new IOException("request was null");

                    switch (request.getMessageType()) {
                        case CONNECT -> createNewSession();
                        case HEARTBEAT -> sendHeartbeatResponse();
                        case DISCONNECT -> {
                            out.writeObject(new Message(MessageType.OK));
                            break runloop;
                        }
                        default -> sendError("Non-implemented request type");
                    }


                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
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
