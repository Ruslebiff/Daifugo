package server;

import protocol.Protocol;

import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.*;

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
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) throws SocketException {
            clientSocket = socket;
            clientSocket.setSoTimeout(Protocol.SOCKET_TIMEOUT);
            LOGGER.addHandler(CONSOLE_HANDLER);
            LOGGER.setLevel(Level.INFO);
            // TODO: add file logging:
            //LOGGER.addHandler(FILE_HANDLER);
        }

        private void sendInvalidRequest() {
            out.println(Protocol.BEGIN_ERROR);
            out.println("Invalid request");
            out.println(Protocol.EOF);
        }

        private void sendError(String msg) {
            out.println(Protocol.BEGIN_ERROR);
            out.println(msg);
            out.println(Protocol.EOF);
        }

        private void sendHeartbeatResponse() {
            long receiveTime = Instant.now().toEpochMilli();
            out.println(Protocol.BEGIN_HEARTBEAT_RESPONSE);
            out.println(Long.toString(receiveTime));
            out.println(Protocol.EOF);
        }

        private void diagnosticHandler(List<String> request) {
            for (String msg : request) {
                out.println(msg);
            }
        }

        private void createNewSession() {
            UserSession session = new UserSession();
            out.println(Protocol.BEGIN_TOKEN);
            out.println(session.getToken());
            out.println(session.getNick());
            out.println(Protocol.EOF);
        }

        public void run() {
            try {
                out = new PrintWriter(
                        clientSocket.getOutputStream(),
                        true
                );
                in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                );
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean connectionIsNew = true;

            String line = "";
            runloop:
            while (true) {
                try {
                    if ((line = in.readLine()) == null) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                assert line != null;
                if (connectionIsNew) {
                    if (!line.equals(Protocol.BEGIN_TOKEN)
                            && !line.equals(Protocol.REQUEST_TOKEN)
                    ) {
                        sendError("You need to identify as first request. Closing connection.");
                        break;
                    }
                    LOGGER.fine("First request of connection");
                    connectionIsNew = false;
                }

                ArrayList<String> request = new ArrayList<>();
                long last = Instant.now().toEpochMilli();
                long now;
                try {
                    do {
                        LOGGER.fine("Start of loop: " + line);
                        now = Instant.now().toEpochMilli();
                        long age = now-last;
                        if (age > Protocol.REQUEST_TIMEOUT) {
                            // Break out of loop and close connection
                            LOGGER.warning("Request timed out");
                            break runloop;
                        }

                        if (Protocol.isMessageSingleInstruction(line)) {
                            LOGGER.fine("Detected single-instruction initiator: " + line);
                            break; // don't look for EOF
                        }

                        out.println(Protocol.ACKNOWLEDGED);
                        request.add(line);

                        try {
                            if ((line = in.readLine()) == null) break;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        last = now;
                    } while (!Objects.equals(line, Protocol.EOF));

                    request.add(line);
                } catch (Exception e) {
                    sendInvalidRequest();
                }

                LOGGER.fine("Passing to handler:");
                for (String s : request) {
                    LOGGER.fine("\t" + s);
                }
                switch (request.get(0)) {
                    case Protocol.REQUEST_TOKEN -> createNewSession();
                    case Protocol.BEGIN_HEARTBEAT -> sendHeartbeatResponse();
                    case Protocol.BEGIN_DIAGNOSTIC -> diagnosticHandler(request);
                    default -> sendError("Unknown request type");
                }


            }

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
