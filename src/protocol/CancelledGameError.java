package protocol;

public class CancelledGameError extends Message {
    public CancelledGameError() {
        super(MessageType.CANCEL_GAME_ERROR);
        errorMessage = "Game has been cancelled by owner";
    }
}
