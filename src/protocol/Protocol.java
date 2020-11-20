package protocol;

/**
 * Protocol supplies a common set of protocol 'constants' for
 * sending messages between the client and server. Should only be used
 * by the networking code.
 */
public class Protocol {

    // Port number
    public static final int PORT = 43273;

    // Timeout in milliseconds
    public static final int SOCKET_TIMEOUT = 10*1000;
}
