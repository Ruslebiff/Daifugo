package protocol;

public enum MessageType {
    ERROR,
    OK,
    HEARTBEAT,
    CONNECT,
    RECONNECT,
    DISCONNECT,
    REQUEST_NICK,
    JOIN_GAME,
    NEW_GAME,
    IDENTITY_RESPONSE,
    GAME_STATE,
}
