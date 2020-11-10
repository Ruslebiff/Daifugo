package protocol;

import java.io.Serializable;

public class Message implements Serializable {
    protected MessageType msgType;

    public Message(MessageType type) {
        msgType = type;
    }

    public MessageType getMessageType() {
        return msgType;
    }
}
