package protocol;

import client.Card;
import common.CardData;

import java.util.List;
import java.util.stream.Collectors;

public class PlayCardsRequest extends Message {
    private List<CardData> cards;

    public PlayCardsRequest(List<Card> cards) {
        super(MessageType.PLAY_CARDS);
        List<CardData> cardDataList = cards
                .stream()
                .map(card -> new CardData(card.getNumber(), card.getSuit()))
                .collect(Collectors.toList());

    }

    public List<CardData> getCards() {
        return cards;
    }
}
