package protocol;

public class JoinGameRequest extends Message {
    private final String gameID;
    private final String password;

    public JoinGameRequest(String gameID, String password) {
        super(MessageType.JOIN_GAME);
        this.gameID = gameID;
        this.password = password;
    }

    public String getGameID() {
        return gameID;
    }

    public String getPassword() {
        return password;
    }
}
