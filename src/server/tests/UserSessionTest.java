package server.tests;

import org.junit.jupiter.api.Test;
import server.UserSession;
import server.exceptions.UserSessionError;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {

    @Test
    public void creatingNewSessionWithUsedTokenShouldFail() {
        String token = UUID.randomUUID().toString();
        UserSession s1 = new UserSession(token);
        assertThrows(UserSessionError.class, () -> new UserSession(token));
    }

}