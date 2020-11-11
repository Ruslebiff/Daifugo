package server.tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import server.UserSession;
import server.exceptions.UserSessionError;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {

    @AfterEach
    public void tearDown() {
        UserSession._reset();   // resets all static variables
    }

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

    @Test
    public void generatedNicksShouldBeConsecutive() {
        UserSession s1 = new UserSession();
        UserSession s2 = new UserSession();
        assertEquals("User1", s1.getNick());
        assertEquals("User2", s2.getNick());
    }

    @Test public void duplicateNickShouldThrowException() {
        UserSession s1 = new UserSession();
        UserSession s2 = new UserSession();
        assertThrows(UserSessionError.class, () -> s2.setNick(s1.getNick()));
    }
}