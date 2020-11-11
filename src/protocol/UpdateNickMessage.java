package protocol;

public class UpdateNickMessage extends IdentityResponse {
    public UpdateNickMessage(String token, String nick) {
        super(token, nick);
        msgType = MessageType.UPDATE_NICK;
    }
}
