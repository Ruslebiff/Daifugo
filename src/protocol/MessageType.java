package protocol;

public enum MessageType {
    ERROR,
    OK,
    CONNECT,
    IDENTITY_RESPONSE,
    DISCONNECT,
    RECONNECT,      // TODO: not currently working -- is it necessary, though?
    HEARTBEAT,
    UPDATE_NICK,
    GET_GAME_LIST,
    GAME_LIST_RESPONSE,
    NEW_GAME,
    JOIN_GAME,
    GAME_STATE,
    START_GAME,
    PLAY_CARDS,
    PASS_TURN,
}
