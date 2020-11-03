package client.networking.tests;

import client.networking.Client;
import common.Protocol;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    @Test
    public void clientHeartbeatShouldReturnReceiveTime() throws IOException {
        Client client = new Client();
        client.connect("localhost", Protocol.PORT);

        long timestamp = Instant.now().toEpochMilli();
        client.sendMessage(Protocol.BEGIN_HEARTBEAT);
        client.sendMessage(Long.toString(timestamp));
        List<String> response = client.sendMessage(Protocol.EOF);
        client.stopConnection();

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