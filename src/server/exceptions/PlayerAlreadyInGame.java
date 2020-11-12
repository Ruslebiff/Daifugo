package server.exceptions;

public class PlayerAlreadyInGame extends GameException {
    public PlayerAlreadyInGame() {
        super("Player already joined");
    }
}
