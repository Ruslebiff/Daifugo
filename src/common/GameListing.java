package common;

import java.io.Serializable;

public class GameListing implements Serializable {
    private final String ID;
    private final String title;
    private final String owner;
    private final int numberOfPlayers;
    private final boolean passwordProtected;
    private final boolean inProgress;


    public GameListing(
            String id,
            String title,
            String owner,
            int numberOfPlayers,
            boolean passwordProtected,
            boolean hasStarted) {
        ID = id;
        this.title = title;
        this.owner = owner;
        this.numberOfPlayers = numberOfPlayers;
        this.passwordProtected = passwordProtected;
        this.inProgress = hasStarted;
    }

    public String getID() {
        return ID;
    }

    public String getOwner() {
        return owner;
    }

    public String getTitle() {
        return title;
    }

    public boolean hasStarted() {
        return inProgress;
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public boolean hasPassword() {
        return passwordProtected;
    }
}
