package protocol;

public class IdentityResponse extends Message {

    public IdentityResponse() {
        super(MessageType.IDENTITY_RESPONSE);
    }

    public String getToken() {
        return "";
    }

    public String getNick() {
        return "";
    }
}
