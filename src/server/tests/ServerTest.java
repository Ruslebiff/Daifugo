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

        conn.disconnect();
    }

    @Test
    public void mustConnectBeforeRequesting() throws IOException, ClassNotFoundException {
        ClientConnection conn = new ClientConnection("localhost");

        Message response = conn.sendMessage(new HeartbeatMessage(
                Instant.now().toEpochMilli())
        );
        assertTrue(response.isError());
        assertEquals("Invalid request", response.getErrorMessage());

        conn.disconnect();
    }

    @Test
    public void getNewNickUpdatesSession() throws IOException, ClassNotFoundException {
        ClientConnection conn = new ClientConnection("localhost");
        Message response = conn.sendMessage(
                new Message(MessageType.CONNECT)
        );
        assertFalse(response.isError());

        IdentityResponse identityResponse = (IdentityResponse) response;
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

        conn.disconnect();
    }

    @Test
    public void creatingNewGameReturnsGameState() throws IOException, ClassNotFoundException {
        ClientConnection conn = new ClientConnection("localhost");
        Message response = conn.sendMessage(new Message(MessageType.CONNECT));
        assertFalse(response.isError());
        char[] pw = {'s', 'e', 'c', 'r', 'e', 't'};
        response = conn.sendMessage(new NewGameMessage(
                "A game title",
                pw
        ));
        assertFalse(response.isError(), response.getErrorMessage());

        assertEquals(MessageType.GAME_STATE, response.getMessageType());

        conn.disconnect();
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


        char[] pw = {}; // empty char array
        response = conn1.sendMessage(new NewGameMessage("first game", pw));
        assertFalse(response.isError(), response.getErrorMessage());

        response = conn1.sendMessage(new Message(MessageType.GET_GAME_LIST));
        assertFalse(response.isError());
        GameListResponse listResponse = (GameListResponse) response;
        assertEquals( 1, listResponse.getGameList().size());

        // only null results in unprotected game:
        assertTrue(listResponse.getGameList().get(0).hasPassword());

        assertEquals("first game",
                listResponse.getGameList().get(0).getTitle()
        );

        char[] pw2 = {'s', 'e', 'c', 'r', 'e', 't'};
        response = conn2.sendMessage(new NewGameMessage("second game", pw2));
        assertFalse(response.isError());

        response = conn3.sendMessage(new Message(MessageType.GET_GAME_LIST));
        assertFalse(response.isError());
        listResponse = (GameListResponse) response;
        List<GameListing> gameList = listResponse.getGameList();
        assertEquals( 2, gameList.size());
        assertEquals("second game", gameList.get(1).getTitle());
        assertTrue(gameList.get(1).hasPassword());
        assertFalse(gameList.get(0).hasStarted());

        conn1.disconnect();
        conn2.disconnect();
        conn3.disconnect();
    }

    @Test
    public void joinGameReturnsGameState() throws IOException, ClassNotFoundException {
        Message response;

        ClientConnection host = new ClientConnection("localhost");
        host.sendMessage(MessageType.CONNECT);
        char[] pw = {'1', '2', '3', '4'};
        response = host.sendMessage(new NewGameMessage("title", pw));
        assertFalse(response.isError(), response.getErrorMessage());

        ClientConnection conn = new ClientConnection("localhost");
        conn.sendMessage(MessageType.CONNECT);
        response = conn.sendMessage(MessageType.GET_GAME_LIST);
        assertFalse(response.isError());
        GameListResponse listResponse = (GameListResponse) response;
        List<GameListing> list = listResponse.getGameList();
        response = conn.sendMessage(new JoinGameRequest(list.get(0).getID(), pw));
        assertFalse(response.isError());
        assertEquals(MessageType.GAME_STATE, response.getMessageType());

        host.disconnect();
        conn.disconnect();
    }

    @Test
    public void supplyingWrongPasswordResultsInPasswordError() throws IOException, ClassNotFoundException {
        Message response;

        ClientConnection host = new ClientConnection("localhost");
        host.sendMessage(MessageType.CONNECT);

        ClientConnection client = new ClientConnection("localhost");
        client.sendMessage(MessageType.CONNECT);

        char[] pw = {'1', '2', '3', '4'};
        response = host.sendMessage(
                new NewGameMessage("title", pw)
        );
        assertFalse(response.isError());

        response = client.sendMessage(MessageType.GET_GAME_LIST);
        assertFalse(response.isError());

        GameListResponse listResponse = (GameListResponse) response;
        String gameID = listResponse.getGameList().get(0).getID();

        char[] pwWrong = {'W', 'r', 'o', 'n', 'g'};
        response = client.sendMessage(
                new JoinGameRequest(gameID, pwWrong)
        );

        assertTrue(response.isError());
        assertEquals(MessageType.PASSWORD_ERROR, response.getMessageType());

        host.disconnect();
        client.disconnect();
    }


    @Test
    public void supplyingNoPasswordShouldNotResultInProtectedGame() throws IOException, ClassNotFoundException {
        ClientConnection host = new ClientConnection("localhost");
        host.sendMessage(MessageType.CONNECT);

        ClientConnection conn = new ClientConnection("localhost");
        conn.sendMessage(MessageType.CONNECT);

        Message response;

        response = host.sendMessage(new NewGameMessage("title", null));
        assertFalse(response.isError());

        response = conn.sendMessage(MessageType.GET_GAME_LIST);
        assertFalse(response.isError());

        GameListResponse listResponse = (GameListResponse) response;
        assertFalse(
                listResponse.getGameList().get(0).hasPassword(),
                "null as password should result in unprotected game"
        );

        // checking if joining works
        String gameID = listResponse.getGameList().get(0).getID();

        response = conn.sendMessage(new JoinGameRequest(gameID, null));
        assertFalse(response.isError());

        host.disconnect();
        conn.disconnect();

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