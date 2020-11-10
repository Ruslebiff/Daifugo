package protocol;

public class HeartbeatMessage extends Message {
    private long timestamp;
    public HeartbeatMessage(long timestamp) {
        super(MessageType.HEARTBEAT);
        this.timestamp = timestamp;
    }

    public long getTime() {
       return timestamp;
    }
}
