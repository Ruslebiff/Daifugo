package client;

import java.util.Deque;

public class Player {
    private final String name;
    private final int playerID;
    private String role;
    private final Deque<Card> cards;

    public Player(String name, int playerID, String role, Deque<Card> cards) {
        this.name = name;
        this.playerID = playerID;
        this.role = role;
        this.cards = cards;
    }

    // Sorts the players hand by using a quicksort
    public void sortHand() {
//        QuickSort.sort(cards, 0, cards.length - 1);
    }

    public String getName(){
        return name;
    }

    public String getRole() {
        return role;
    }
}
