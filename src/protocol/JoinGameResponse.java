package protocol;

import java.util.UUID;

// TODO: DEPRECATED -- DELETE

public class JoinGameResponse extends Message {
    private String gameID;

    public JoinGameResponse(String gameID) {
        super(MessageType.JOIN_GAME_RESPONSE);
    }

    public String getGameID() {
        return gameID;
    }
}
