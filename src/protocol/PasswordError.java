package protocol;

public class PasswordError extends Message {
    public PasswordError() {
        super(MessageType.PASSWORD_ERROR);
    }
}
