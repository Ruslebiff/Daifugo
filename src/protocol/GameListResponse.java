package protocol;

import common.GameListing;

import java.util.List;

public class GameListResponse extends Message {
    private final List<GameListing> gameList;
    public GameListResponse(
            List<GameListing> gameList
    ) {
        super(MessageType.GAME_LIST_RESPONSE);
        this.gameList = gameList;
    }

    public List<GameListing> getGameList() {
        return gameList;
    }
}
