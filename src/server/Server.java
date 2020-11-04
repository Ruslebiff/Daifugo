package server;

import common.Protocol;

import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private ServerSocket serverSocket;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (true)
            new Server.ClientHandler(serverSocket.accept()).start();
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        System.out.printf("Daifugo server starting on port %d...\n", Protocol.PORT);
        server.start(Protocol.PORT);


    }

    private static class ClientHandler extends Thread {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) throws SocketException {
            clientSocket = socket;
            clientSocket.setSoTimeout(Protocol.SOCKET_TIMEOUT *1000);
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

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                );
            } catch (IOException e) {
                e.printStackTrace();
            }

            String line = "";
            while (true) {
                try {
                    if ((line = in.readLine()) == null) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ArrayList<String> request = new ArrayList<>();
                try {
                    do {
                        request.add(line);
                        out.println(Protocol.ACKNOWLEDGED);
                        line = in.readLine();
                    } while (!line.equals(Protocol.EOF));
                    request.add(line);
                } catch (IOException e) {
                    sendInvalidRequest();
                }

                switch (request.get(0)) {
                    case Protocol.BEGIN_HEARTBEAT -> sendHeartbeatResponse();
                    case Protocol.BEGIN_DIAGNOSTIC -> diagnosticHandler(request);
                    default -> sendError("Unknown request type");
                }


            }

            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
