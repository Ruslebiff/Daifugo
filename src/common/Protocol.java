package common;

/**
 * Protocol supplies a common set of protocol 'constants' for
 * sending messages between the client and server. Should only be used
 * by the networking code.
 */
public class Protocol {

    // Message terminator
    public static final String EOF = "EOF";

    // Message initiators
    public static final String BEGIN_NEW_NICK = "BGNNICK";
    public static final String BEGIN_NEW_GAME = "BGNNG";
    public static final String BEGIN_JOIN_GAME = "BGNJOIN";
    public static final String BEGIN_HEARTBEAT = "BGNHB";
    public static final String BEGIN_HEARTBEAT_RESPONSE = "BGNHBR";
    public static final String BEGIN_ERROR = "BGNE";
    public static final String BEGIN_TOKEN = "BGNTKN";
    public static final String BEGIN_DIAGNOSTIC = "BGNDIAG"; // return same data as sent

    // Single-line message codes
    public static final String ACKNOWLEDGED = "ACK";
    public static final String REQUEST_TOKEN = "RQTKN";
    public static final String CLOSE_CONNECTION = "DONE";

    public static boolean isMessageSingleInstruction(String msg) {
        return msg.equals(ACKNOWLEDGED)
                || msg.equals(REQUEST_TOKEN)
                || msg.equals(CLOSE_CONNECTION);
    }

    // Port number
    public static final int PORT = 5555;


    // Timeout in milliseconds
    public static final int SOCKET_TIMEOUT = 10*1000;
    public static final int REQUEST_TIMEOUT = 3*1000;   // timeout for between BEGIN_* and EOF


}
