package client.networking.tests;

import client.networking.ClientConnection;
import common.Protocol;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientConnectionTest {

    @Test
    public void connectAndDisconnectShouldntThrowException() throws IOException {
        ClientConnection conn = new ClientConnection();
        conn.connect("localhost", Protocol.PORT);
        assertDoesNotThrow(conn::disconnect);
    }

    @Test
    public void clientHeartbeatShouldReturnReceiveTime() throws IOException {
        ClientConnection clientConnection = new ClientConnection();
        clientConnection.connect("localhost", Protocol.PORT);

        long timestamp = Instant.now().toEpochMilli();
        clientConnection.sendMessage(Protocol.BEGIN_HEARTBEAT);
        clientConnection.sendMessage(Long.toString(timestamp));
        List<String> response = clientConnection.sendMessage(Protocol.EOF);
        clientConnection.disconnect();

        assertEquals(response.get(0), Protocol.BEGIN_HEARTBEAT_RESPONSE);
        long now = Instant.now().toEpochMilli();
        long receivedTime = Long.parseLong(response.get(1));
        assertTrue(
                timestamp <= receivedTime
                && receivedTime <= now
        );

        assertEquals(response.get(2), Protocol.EOF);
    }

    @Test
    public void parallellRequestsShouldNotInterfereEachother() throws IOException {
        // connecting parallell clients
        ClientConnection clientOne = new ClientConnection();
        clientOne.connect("localhost", Protocol.PORT);
        ClientConnection clientTwo = new ClientConnection();
        clientTwo.connect("localhost", Protocol.PORT);
        ClientConnection clientThree = new ClientConnection();
        clientThree.connect("localhost", Protocol.PORT);

        clientOne.sendMessage(Protocol.BEGIN_DIAGNOSTIC);
        clientTwo.sendMessage(Protocol.BEGIN_DIAGNOSTIC);
        clientThree.sendMessage(Protocol.BEGIN_DIAGNOSTIC);

        clientTwo.sendMessage("c2 first");
        clientOne.sendMessage("c1 first");
        clientTwo.sendMessage("c2 second");
        clientThree.sendMessage("c3 first");
        clientTwo.sendMessage("c2 third");
        clientOne.sendMessage("c1 second");
        clientThree.sendMessage("c3 second");
        clientThree.sendMessage("c3 third");
        clientThree.sendMessage(Protocol.EOF);
        clientTwo.sendMessage(Protocol.EOF);
        clientOne.sendMessage("c1 third");
        List<String> response = clientOne.sendMessage(Protocol.EOF);

        assertEquals(Protocol.BEGIN_DIAGNOSTIC, response.get(0));
        assertEquals("c1 first", response.get(1));
        assertEquals("c1 second", response.get(2));
        assertEquals("c1 third", response.get(3));
        assertEquals(Protocol.EOF, response.get(4));

        clientOne.disconnect();
        clientTwo.disconnect();
        clientThree.disconnect();
    }

}