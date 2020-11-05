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


}