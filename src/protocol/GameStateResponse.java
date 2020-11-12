package protocol;

import common.GameState;
import server.Game;
import server.UserSession;
import server.exceptions.UserSessionError;


public class GameStateResponse extends Message {
    private GameState state;

    public GameStateResponse(Game game, UserSession playerSession) throws UserSessionError {
        super(MessageType.GAME_STATE);

        state = new GameState(game, playerSession);
    }
}
