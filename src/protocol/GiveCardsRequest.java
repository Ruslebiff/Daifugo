package protocol;

import client.Card;
import common.CardData;

import java.util.List;
import java.util.stream.Collectors;

public class GiveCardsRequest extends Message {

    private List<CardData> cards;

    public GiveCardsRequest(List<Card> cards) {
        super(MessageType.GIVE_CARDS);

        List<CardData> cardDataList = cards
                .stream()
                .map(card -> new CardData(card.getNumber(), card.getSuit()))
                .collect(Collectors.toList());

            this.cards = cardDataList;
    }

    public List<CardData> getCards() {
        return cards;
    }
}
