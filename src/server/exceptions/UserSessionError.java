package server.exceptions;

public class UserSessionError extends Exception {
    public UserSessionError() {
        super();
    }
    public UserSessionError(String msg) {
        super(msg);
    }
}
