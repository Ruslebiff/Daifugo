package protocol;

public class PasswordError extends Message {
    public PasswordError() {
        super(MessageType.PASSWORD_ERROR);
    }
}

//TODO: send this if joining with incorrect password