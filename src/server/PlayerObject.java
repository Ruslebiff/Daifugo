package server;

import common.PlayerData;

import static server.Server.SERVER_LOGGER;

public class PlayerObject {
    private final UserSession session;
    private PlayerData gameData;
    private boolean stateUpdated;

    public PlayerObject(UserSession session, PlayerData gameData) {
        this.session = session;
        this.gameData = gameData;
        stateUpdated = true;
    }

    public void stateHasBeenSent() {
        synchronized (this) {
            stateUpdated = false;
        }
    }

    public void newStateAvailable() {
        synchronized (this) {
            stateUpdated = true;
        }
    }

    public PlayerData getGameData() {
        return this.gameData;
    }

    public UserSession getSession() {
        return session;
    }

    public void setGameData(PlayerData gameData) {
        this.gameData = gameData;
    }

    public boolean isStateUpdated() {
        synchronized (this) {
            return stateUpdated;
        }
    }
}
