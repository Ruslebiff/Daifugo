package protocol;

public enum MessageType {
    ERROR,
    PASSWORD_ERROR,
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
    JOIN_GAME_RESPONSE,
    GAME_STATE,
    CANCEL_GAME,    // owner ends game
    CANCEL_GAME_ERROR,      // propagated to players if a game is cancelled
    START_GAME,     // owner starts game
    LEAVE_GAME,     // a player leaving a game that may or may not be started
    PLAY_CARDS,
    PASS_TURN,
    GIVE_CARDS,     // trade cards between presidents and bums
}
