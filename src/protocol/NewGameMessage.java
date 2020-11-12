package protocol;

public class NewGameMessage extends Message {
    private final String title;
    private final String password;

    public NewGameMessage(String title, String password) {
        super(MessageType.NEW_GAME);
        this.title = title;
        this.password = password;
    }

    public String getTitle() {
        return title;
    }

    public String getPassword() {
        return password;
    }
}
