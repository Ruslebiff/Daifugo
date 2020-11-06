package server.tests;

import org.junit.jupiter.api.Test;
import server.UserSession;
import server.exceptions.UserSessionError;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {

    @Test
    public void creatingNewSessionWithUsedTokenShouldFail() throws UserSessionError {
        UserSession s1 = new UserSession();
        assertThrows(UserSessionError.class, () -> new UserSession(s1.getToken()));
    }

    @Test
    public void gettingSessionByTokenResultsInSameInstance() throws UserSessionError {
        UserSession s1 = new UserSession();
        String token = s1.getToken();

        UserSession s2 = UserSession.retrieveSessionFromToken(token);

        assertSame(s1, s2);
    }

    @Test
    public void retrievingNonexistingSessionShouldThrow() {
        String token = UUID.randomUUID().toString();
        assertThrows(
                UserSessionError.class,
                () -> UserSession.retrieveSessionFromToken(token)
        );
    }
}