package server.tests;

import client.networking.ClientConnection;
import protocol.*;
import org.junit.jupiter.api.*;
import server.Server;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {

    private static Server server;

    @BeforeEach
    void startLocalServer() throws InterruptedException {
        server = new Server();
        new Thread(() -> {
            try {
                server.start(Protocol.PORT);
            } catch (Exception ignored) {
            }
        }).start();

        Thread.sleep(1000);
    }

    @AfterEach
    void stopLocalServer() throws IOException {
        server.stop();
    }

    @Test
    public void clientHeartbeatShouldReturnReceiveTime() throws IOException, ClassNotFoundException {
        ClientConnection clientConnection = new ClientConnection("localhost");

        // starting session
        clientConnection.sendMessage(new Message(MessageType.CONNECT));

        long timestamp = Instant.now().toEpochMilli();
        HeartbeatMessage response = (HeartbeatMessage) clientConnection.sendMessage(
                new HeartbeatMessage(timestamp)
        );
        clientConnection.disconnect();

        assertEquals(response.getMessageType(), MessageType.HEARTBEAT);
        long now = Instant.now().toEpochMilli();
        long receivedTime = response.getTime();
        assertTrue(
                timestamp <= receivedTime
                        && receivedTime <= now
        );
    }

    @Test
    public void connectAndDisconnectShouldNotThrowException() throws IOException {
        ClientConnection conn = new ClientConnection("localhost");
        assertDoesNotThrow(conn::disconnect);
    }

    @Test
    public void newConnectionSuppliesTokenAndUserName() throws IOException, ClassNotFoundException {
        ClientConnection conn = new ClientConnection("localhost");

        IdentityResponse response = (IdentityResponse) conn.sendMessage(
                new Message(MessageType.CONNECT)
        );
        assertEquals(MessageType.IDENTITY_RESPONSE, response.getMessageType());
        assertEquals("User1", response.getNick());
    }

    @Test
    public void mustConnectBeforeRequesting() throws IOException, ClassNotFoundException {
        ClientConnection conn = new ClientConnection("localhost");

        Message response = conn.sendMessage(new HeartbeatMessage(
                Instant.now().toEpochMilli())
        );
        assertTrue(response.isError());
        assertEquals("Invalid request", response.getErrorMessage());
    }

}