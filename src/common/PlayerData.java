package common;

import java.util.ArrayList;
import java.util.List;

public class PlayerData {
    private String nick;
    private long latency;
    private int numberOfCards;
    private boolean connectionLost;
    private int role;
    private boolean passed;
    private boolean outOfRound;
    private int outCount;
    private List<Integer> previousRoles;

    public PlayerData(
            String nick,
            int numberOfCards,
            boolean passed,
            int role,
            int latency
    ) {
       this.nick = nick;
       this.passed = passed;
       this.numberOfCards = numberOfCards;
       this.role = role;
       this.latency = latency;

       outCount = 0;
       outOfRound = false;
       previousRoles = new ArrayList<>();

       // negative latency value is lost connection
       connectionLost = latency < 0;
    }

    public String getNick() {
        return nick;
    }

    public long getLatency() {
        return latency;
    }

    public int getNumberOfCards() {
        return numberOfCards;
    }

    public int getRole() {
        return role;
    }


    public void setConnectionLost(boolean connectionLost) {
        this.connectionLost = connectionLost;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setNumberOfCards(int numberOfCards) {
        this.numberOfCards = numberOfCards;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public boolean hasPassed() {
        return passed;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public void setOutCount(int outCount) {
        this.outCount = outCount;
    }

    public int getOutCount() {
        return outCount;
    }

    public void setOutOfRound(boolean outOfRound) {
        this.outOfRound = outOfRound;
    }

    public boolean isOutOfRound() {
        return outOfRound;
    }


}
