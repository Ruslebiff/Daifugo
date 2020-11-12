package server.exceptions;

public class GameInProgress extends GameException {
    public GameInProgress() {
        super("Game is in progress");
    }
}
