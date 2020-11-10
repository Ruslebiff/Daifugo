package protocol;

public enum MessageType {
    ERROR,
    OK,
    CONNECT,
    IDENTITY_RESPONSE,
    DISCONNECT,
    RECONNECT,      // TODO: not currently working -- is it necessary? though?
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
