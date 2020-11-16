package protocol;

public class JoinGameRequest extends Message {
    private final String gameID;
    private final char[] password;

    public JoinGameRequest(String gameID, char[] password) {
        super(MessageType.JOIN_GAME);
        this.gameID = gameID;
        this.password = password;
    }

    public String getGameID() {
        return gameID;
    }

    public char[] getPassword() {
        return password;
    }
}
