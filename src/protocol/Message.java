package protocol;

import java.io.Serializable;

public class Message implements Serializable {
    protected MessageType msgType;
    protected String errorMessage;

    public Message(MessageType type) {
        msgType = type;
        errorMessage = null;
    }

    public MessageType getMessageType() {
        return msgType;
    }

    public boolean isError() {
       return msgType == MessageType.ERROR;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
