package client.networking.tests;

import client.networking.ClientConnection;
import protocol.ErrorMessage;
import protocol.Message;
import protocol.MessageType;
import protocol.Protocol;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import server.Server;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ClientConnectionTest {

    private static Server server;

    @BeforeAll
    static void startLocalServer() throws InterruptedException {
        server = new Server();
        new Thread(() -> {
            try {
                server.start(Protocol.PORT);
            } catch (Exception ignored) {
            }
        }).start();

        Thread.sleep(1000);
    }

    @AfterAll
    static void stopLocalServer() throws IOException {
        server.stop();
    }

    /**
     * I have opted out of using an exception, since that may expose
     * server information down the line if users of ClientConnection
     * were to make errors in sending exception text unfiltered to users
     */
    @Test
    public void sendingErrorMessageToServerShouldNotBeAccepted() throws ClassNotFoundException, IOException {

        ClientConnection connection = new ClientConnection("localhost");
        connection.sendMessage(new Message(MessageType.CONNECT));
        Message response = connection.sendMessage(new ErrorMessage("test"));

        assertEquals(MessageType.ERROR, response.getMessageType());
        assertEquals("Non-implemented request type", response.getErrorMessage());
    }

}