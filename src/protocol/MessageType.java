package protocol;

public enum MessageType {
    ERROR,
    OK,
    CONNECT,
    IDENTITY_RESPONSE,
    DISCONNECT,
    RECONNECT,
    HEARTBEAT,
    UPDATE_NICK,
    GET_GAME_LIST,
    JOIN_GAME,
    NEW_GAME,
    GAME_STATE,
    PLAY_CARDS,
    PASS_TURN,
    START_GAME,
}
