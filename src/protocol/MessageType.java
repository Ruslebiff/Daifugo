package protocol;

public enum MessageType {
    ERROR,
    OK,
    HEARTBEAT,
    CONNECT,
    RECONNECT,
    DISCONNECT,
    REQUEST_NICK,
    REQUEST_GAME_LIST,
    JOIN_GAME,
    NEW_GAME,
    IDENTITY_RESPONSE,
    GAME_STATE,
    PLAY_CARDS,
    PASS_TURN,
    START_GAME,
}
