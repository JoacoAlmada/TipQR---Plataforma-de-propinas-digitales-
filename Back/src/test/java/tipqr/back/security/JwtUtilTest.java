package tipqr.back.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRET = "tipqr-super-secret-key-for-tests-must-be-at-least-32-chars-ok";
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, 86400000L);
    }

    private UserDetails buildUser(String username, String role) {
        return User.builder().username(username).password("pass").roles(role).build();
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken(buildUser("test@tipqr.com", "DUENO"));
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractUsername_returnsCorrectUsername() {
        UserDetails user = buildUser("admin@tipqr.com", "DUENO");
        String token = jwtUtil.generateToken(user);
        assertEquals("admin@tipqr.com", jwtUtil.extractUsername(token));
    }

    @Test
    void isTokenValid_withValidToken_returnsTrue() {
        UserDetails user = buildUser("admin@tipqr.com", "DUENO");
        String token = jwtUtil.generateToken(user);
        assertTrue(jwtUtil.isTokenValid(token, user));
    }

    @Test
    void isTokenValid_withDifferentUser_returnsFalse() {
        UserDetails user1 = buildUser("user1@tipqr.com", "DUENO");
        UserDetails user2 = buildUser("user2@tipqr.com", "EMPLEADO");
        String token = jwtUtil.generateToken(user1);
        assertFalse(jwtUtil.isTokenValid(token, user2));
    }

    @Test
    void isTokenValid_withExpiredToken_throwsException() throws InterruptedException {
        JwtUtil shortLived = new JwtUtil(SECRET, 1L);
        UserDetails user = buildUser("test@tipqr.com", "DUENO");
        String token = shortLived.generateToken(user);
        Thread.sleep(20);
        assertThrows(Exception.class, () -> shortLived.isTokenValid(token, user));
    }
}
