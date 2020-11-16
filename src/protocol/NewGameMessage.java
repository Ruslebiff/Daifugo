package protocol;

public class NewGameMessage extends Message {
    private final String title;
    private final char[] password;

    public NewGameMessage(String title, char[] password) {
        super(MessageType.NEW_GAME);
        this.title = title;
        this.password = password;
    }

    public String getTitle() {
        return title;
    }

    public char[] getPassword() {
        return password;
    }
}
