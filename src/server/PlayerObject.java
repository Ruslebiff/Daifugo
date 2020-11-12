package server;

import common.PlayerData;

import java.io.Serializable;

public class PlayerObject implements Serializable {
    private UserSession session;
    private PlayerData gameData;

    public PlayerObject(UserSession session, PlayerData gameData) {
        this.session = session;
        this.gameData = gameData;
    }

    public PlayerData getGameData() {
        return gameData;
    }

    public UserSession getSession() {
        return session;
    }

    public void setGameData(PlayerData gameData) {
        this.gameData = gameData;
    }
}
