package protocol;

public class ErrorMessage extends Message {

    private String message;

    public ErrorMessage(String message) {
        super(MessageType.ERROR);
        this.message = message;
    }

    public String getErrorString() {
        return message;
    }
}
