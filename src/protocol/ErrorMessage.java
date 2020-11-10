package protocol;

public class ErrorMessage extends Message {
    public ErrorMessage(String message) {
        super(MessageType.ERROR);
        this.errorMessage = message;
    }
}
