package server.tests;

import client.networking.ClientConnection;
import common.GameListing;
import protocol.*;
import org.junit.jupiter.api.*;
import server.Server;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

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

    @Test
    public void getNewNickUpdatesSession() throws IOException, ClassNotFoundException {
        ClientConnection conn = new ClientConnection("localhost");
        Message response = conn.sendMessage(
                new Message(MessageType.CONNECT)
        );
        assertFalse(response.isError());

        IdentityResponse identityResponse = (IdentityResponse) response;
        String oldNick = identityResponse.getNick();
        String newNick = "John";

        response = conn.sendMessage(
                new UpdateNickMessage(identityResponse.getToken(), newNick)
        );
        assertFalse(
                response.isError(),
                "Updating nick shall not result in error"
        );
        IdentityResponse updatedNickResponse = (IdentityResponse) response;
        String got = updatedNickResponse.getNick();

        assertEquals(newNick, got);
    }

    @Test
    public void creatingNewGameReturnsJoinGameResponse() throws IOException, ClassNotFoundException {
        ClientConnection conn = new ClientConnection("localhost");
        Message response = conn.sendMessage(new Message(MessageType.CONNECT));
        assertFalse(response.isError());

        response = conn.sendMessage(new NewGameMessage(
                "A game title",
                "secret"
        ));
        assertFalse(response.isError(), "New game should not return error");

        assertEquals(MessageType.JOIN_GAME_RESPONSE, response.getMessageType());


    }

    @Test
    public void retrievingGameListReturnsCorrectly() throws IOException, ClassNotFoundException {
        ClientConnection conn1 = new ClientConnection("localhost");
        Message response = conn1.sendMessage(new Message(MessageType.CONNECT));
        assertFalse(response.isError());

        ClientConnection conn2 = new ClientConnection("localhost");
        response = conn2.sendMessage(new Message(MessageType.CONNECT));
        assertFalse(response.isError());

        ClientConnection conn3 = new ClientConnection("localhost");
        response = conn3.sendMessage(new Message(MessageType.CONNECT));
        assertFalse(response.isError());


        response = conn1.sendMessage(new NewGameMessage("first game", ""));
        assertFalse(response.isError());

        response = conn1.sendMessage(new Message(MessageType.GET_GAME_LIST));
        assertFalse(response.isError());
        GameListResponse listResponse = (GameListResponse) response;
        assertEquals( 1, listResponse.getGameList().size());
        assertEquals("first game",
                listResponse.getGameList().get(0).getTitle()
        );

        response = conn2.sendMessage(new NewGameMessage("second game", "secret"));
        assertFalse(response.isError());

        response = conn3.sendMessage(new Message(MessageType.GET_GAME_LIST));
        assertFalse(response.isError());
        listResponse = (GameListResponse) response;
        List<GameListing> gameList = listResponse.getGameList();
        assertEquals( 2, gameList.size());
        assertEquals("second game", gameList.get(1).getTitle());
        assertTrue(gameList.get(1).hasPassword());
        assertFalse(gameList.get(0).hasStarted());

    }

    // TODO: find out why this results in timeouts
/*
    @Test
    public void reconnectSucceedsWhenReestablishingConnection() throws IOException, ClassNotFoundException {
        ClientConnection conn = new ClientConnection("localhost");


        Message response = conn.sendMessage(
            new Message(MessageType.CONNECT)
        );
        if (response.isError()) {}






        IdentityResponse identityResponse = (IdentityResponse) response;

        String token = identityResponse.getToken();
        String nick = identityResponse.getNick();

        response = conn.sendMessage(new Message(MessageType.DISCONNECT));
        assertEquals(MessageType.OK, response.getMessageType());

        //conn.disconnect();

        conn = new ClientConnection("localhost");

        identityResponse = (IdentityResponse) conn.sendMessage(
                new ReconnectMessage(token)
        );

        assertEquals(token, identityResponse.getToken());
        assertEquals(nick, identityResponse.getNick());
    }
*/

}