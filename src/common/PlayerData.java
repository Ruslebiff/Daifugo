package common;

public class PlayerData {
    private String nick;
    private int latency;
    private int numberOfCards;
    private boolean connectionLost;
    private int role;
    private boolean passed;

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

       // negative latency value is lost connection
       connectionLost = latency < 0;
    }

    public String getNick() {
        return nick;
    }

    public int getLatency() {
        return latency;
    }

    public int getNumberOfCards() {
        return numberOfCards;
    }

    public int getRole() {
        return role;
    }

    public boolean hasPassed() {
        return passed;
    }

    public void setConnectionLost(boolean connectionLost) {
        this.connectionLost = connectionLost;
    }

    public void setLatency(int latency) {
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

    public void setRole(int role) {
        this.role = role;
    }
}
