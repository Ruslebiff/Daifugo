package protocol;

import client.Card;

import java.util.List;

public class GiveCardsRequest extends PlayCardsRequest {
    public GiveCardsRequest(List<Card> cards) {
        super(cards);
        msgType = MessageType.GIVE_CARDS;
    }
}
