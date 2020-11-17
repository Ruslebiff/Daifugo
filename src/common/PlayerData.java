package common;

import java.util.*;

public class PlayerData {
    private String nick;
    private long latency;
    private int numberOfCards;
    private boolean connectionLost;
    private Role role;
    private boolean passed;
    private boolean outOfRound;
    private int outCount;
    private List<Role> previousRoles;

    public PlayerData(
            String nick,
            int numberOfCards,
            boolean passed,
            Role role,
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

    public void reset() {
        role = Role.NEUTRAL;
        previousRoles = new ArrayList<>();
        passed = false;
        outOfRound = false;
        outCount = 0;
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

    public Role getRole() {
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

    public void setRole(Role role) {
        this.role = role;
    }

    public void setOutCount(int outCount) {
        this.outCount = outCount;
        outOfRound = true;
    }

    public int getOutCount() {
        return outCount;
    }


    public boolean isOutOfRound() {
        return outOfRound;
    }

    public void assignRoleFewPlayers() {
        if (outCount == 1) {
            role = Role.PRESIDENT;
        } else if (outCount == 3) {
            role = Role.BUM;
        } else {
            role = Role.NEUTRAL;
        }
        previousRoles.add(role);
    }


    public void assignRoleManyPlayers(int playerAmount) {
        if (outCount == 1) {
            role = Role.PRESIDENT;
        } else if (outCount == 2) {
            role = Role.VICE_PRESIDENT;
        } else if (outCount == playerAmount - 1) {
            role = Role.VICE_BUM;
        } else if (outCount == playerAmount) {
            role = Role.BUM;
        } else {
            role = Role.NEUTRAL;
        }
        previousRoles.add(role);
    }

}
