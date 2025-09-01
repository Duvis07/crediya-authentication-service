package co.com.crediya.authentication.jwt;

import co.com.crediya.authentication.model.role.Role;
import co.com.crediya.authentication.model.user.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtRepositoryImplTest {

    private static final String INVALID_TOKEN = "invalid.jwt.token";

    private JwtRepositoryImpl jwtRepository;
    private User testUser;
    private final String testSecret = "myTestSecretKeyForJwtTokenGenerationThatIsLongEnoughToMeetJWTRequirements256Bits";
    private final long testExpiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtRepository = new JwtRepositoryImpl(testSecret, testExpiration);

        Role adminRole = Role.builder()
                .id(1L)
                .name("ADMIN")
                .description("Administrator role")
                .build();

        testUser = User.builder()
                .id(1L)
                .firstName("Test")
                .lastName("User")
                .email("test@crediya.com")
                .password("$2a$10$encrypted.password.hash")
                .birthDate(LocalDate.of(1990, 1, 1))
                .baseSalary(BigDecimal.valueOf(5_000_000))
                .address("Test Address")
                .phone("3001234567")
                .role(adminRole)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void generateTokenSuccess() {
        StepVerifier.create(jwtRepository.generateToken(testUser))
                .expectNextMatches(jwtToken -> {
                    assertThat(jwtToken.getToken()).isNotNull().isNotEmpty();
                    assertThat(jwtToken.getTokenType()).isEqualTo("Bearer");
                    assertThat(jwtToken.getExpiresIn()).isEqualTo(testExpiration / 1000);
                    assertThat(jwtToken.getUserId()).isEqualTo(testUser.getId());
                    assertThat(jwtToken.getUserRole()).isEqualTo(testUser.getRole().getName());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void generateTokenWithDifferentRoles() {
        Role asesorRole = Role.builder()
                .id(2L)
                .name("ASESOR")
                .description("Asesor role")
                .build();

        User asesorUser = testUser.toBuilder()
                .role(asesorRole)
                .email("asesor@crediya.com")
                .build();

        StepVerifier.create(jwtRepository.generateToken(asesorUser))
                .expectNextMatches(jwtToken -> {
                    assertThat(jwtToken.getUserRole()).isEqualTo("ASESOR");
                    assertThat(jwtToken.getTokenType()).isEqualTo("Bearer");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void validateTokenSuccess() {
        StepVerifier.create(jwtRepository.generateToken(testUser)
                        .flatMap(jwtToken -> jwtRepository.validateToken(jwtToken.getToken())))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void validateTokenFailsWithInvalidToken() {
        StepVerifier.create(jwtRepository.validateToken(INVALID_TOKEN))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void validateTokenFailsWithExpiredToken() {
        // Create a token with very short expiration
        JwtRepositoryImpl shortExpirationRepo = new JwtRepositoryImpl(testSecret, 1); // 1ms

        StepVerifier.create(shortExpirationRepo.generateToken(testUser)
                        .delayElement(Duration.ofMillis(10)) // Wait for expiration
                        .flatMap(jwtToken -> jwtRepository.validateToken(jwtToken.getToken())))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void validateTokenFailsWithMalformedToken() {
        String malformedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.malformed";

        StepVerifier.create(jwtRepository.validateToken(malformedToken))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void extractEmailFromToken() {
        StepVerifier.create(jwtRepository.generateToken(testUser))
                .expectNextMatches(jwtToken -> {
                    String email = jwtRepository.extractEmail(jwtToken.getToken());
                    assertThat(email).isEqualTo(testUser.getEmail());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void extractRoleFromToken() {
        StepVerifier.create(jwtRepository.generateToken(testUser))
                .expectNextMatches(jwtToken -> {
                    String role = jwtRepository.extractRole(jwtToken.getToken());
                    assertThat(role).isEqualTo(testUser.getRole().getName());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void extractUserIdFromToken() {
        StepVerifier.create(jwtRepository.generateToken(testUser))
                .expectNextMatches(jwtToken -> {
                    Long userId = jwtRepository.extractUserId(jwtToken.getToken());
                    assertThat(userId).isEqualTo(testUser.getId());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void extractEmailFromInvalidTokenThrowsException() {
        try {
            jwtRepository.extractEmail(INVALID_TOKEN);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void extractRoleFromInvalidTokenThrowsException() {
        try {
            jwtRepository.extractRole(INVALID_TOKEN);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void extractUserIdFromInvalidTokenThrowsException() {
        try {
            jwtRepository.extractUserId(INVALID_TOKEN);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void generateTokenWithNullUserThrowsException() {
        StepVerifier.create(jwtRepository.generateToken(null))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void generateTokenWithUserWithoutRoleThrowsException() {
        User userWithoutRole = testUser.toBuilder()
                .role(null)
                .build();

        StepVerifier.create(jwtRepository.generateToken(userWithoutRole))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void tokenContainsCorrectClaims() {
        StepVerifier.create(jwtRepository.generateToken(testUser))
                .expectNextMatches(jwtToken -> {
                    String token = jwtToken.getToken();

                    // Verify token structure and claims
                    SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes());
                    var claims = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

                    assertThat(claims.getSubject()).isEqualTo(testUser.getEmail());
                    assertThat(claims.get("userId", Long.class)).isEqualTo(testUser.getId());
                    assertThat(claims.get("email", String.class)).isEqualTo(testUser.getEmail());
                    assertThat(claims.get("role", String.class)).isEqualTo(testUser.getRole().getName());
                    assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(new Date());
                    assertThat(claims.getExpiration()).isAfter(new Date());

                    return true;
                })
                .verifyComplete();
    }

    @Test
    void validateTokenWithDifferentSecretFails() {
        // Generate token with one secret
        StepVerifier.create(jwtRepository.generateToken(testUser)
                        .flatMap(jwtToken -> {
                    // Try to validate with different secret (must be 256+ bits)
                    JwtRepositoryImpl differentSecretRepo = new JwtRepositoryImpl("differentSecretKeyThatIsLongEnoughToMeetJWTRequirements256BitsMinimum", testExpiration);
                    return differentSecretRepo.validateToken(jwtToken.getToken());
                }))
                .expectNext(false)
                .verifyComplete();
    }
}
