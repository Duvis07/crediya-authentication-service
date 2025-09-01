package co.com.crediya.authentication.usecase;

import co.com.crediya.authentication.model.auth.JwtToken;
import co.com.crediya.authentication.model.auth.gateways.JwtRepository;
import co.com.crediya.authentication.model.auth.gateways.PasswordEncoderRepository;
import co.com.crediya.authentication.model.exceptions.InvalidCredentialsException;
import co.com.crediya.authentication.model.role.Role;
import co.com.crediya.authentication.model.user.User;
import co.com.crediya.authentication.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthUseCaseTest {

    private UserRepository userRepository;
    private JwtRepository jwtRepository;
    private PasswordEncoderRepository passwordEncoderRepository;
    private AuthUseCase authUseCase;

    private User validUser;
    private JwtToken validToken;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        jwtRepository = Mockito.mock(JwtRepository.class);
        passwordEncoderRepository = Mockito.mock(PasswordEncoderRepository.class);
        authUseCase = new AuthUseCase(userRepository, jwtRepository, passwordEncoderRepository);

        Role adminRole = Role.builder()
                .id(1L)
                .name("ADMIN")
                .description("Administrator role")
                .build();

        validUser = User.builder()
                .id(1L)
                .firstName("Admin")
                .lastName("User")
                .email("admin@crediya.com")
                .password("$2a$10$encrypted.password.hash")
                .birthDate(LocalDate.of(1985, 1, 1))
                .baseSalary(BigDecimal.valueOf(8_000_000))
                .address("Admin Address")
                .phone("3001234567")
                .role(adminRole)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validToken = JwtToken.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .userRole("ADMIN")
                .build();
    }

    @Test
    void loginSuccess() {
        when(userRepository.findByEmail("admin@crediya.com")).thenReturn(Mono.just(validUser));
        when(passwordEncoderRepository.matches("admin123456", validUser.getPassword())).thenReturn(Mono.just(true));
        when(jwtRepository.generateToken(validUser)).thenReturn(Mono.just(validToken));

        StepVerifier.create(authUseCase.login("admin@crediya.com", "admin123456"))
                .expectNextMatches(token -> 
                    token.getToken().equals(validToken.getToken()) &&
                    token.getTokenType().equals("Bearer") &&
                    token.getUserRole().equals("ADMIN"))
                .verifyComplete();

        verify(userRepository).findByEmail("admin@crediya.com");
        verify(passwordEncoderRepository).matches("admin123456", validUser.getPassword());
        verify(jwtRepository).generateToken(validUser);
    }

    @Test
    void loginFailsWhenUserNotFound() {
        when(userRepository.findByEmail("nonexistent@crediya.com")).thenReturn(Mono.empty());

        StepVerifier.create(authUseCase.login("nonexistent@crediya.com", "password"))
                .expectError(InvalidCredentialsException.class)
                .verify();

        verify(userRepository).findByEmail("nonexistent@crediya.com");
        verify(passwordEncoderRepository, never()).matches(anyString(), anyString());
        verify(jwtRepository, never()).generateToken(any(User.class));
    }

    @Test
    void loginFailsWhenPasswordIsInvalid() {
        when(userRepository.findByEmail("admin@crediya.com")).thenReturn(Mono.just(validUser));
        when(passwordEncoderRepository.matches("wrongpassword", validUser.getPassword())).thenReturn(Mono.just(false));

        StepVerifier.create(authUseCase.login("admin@crediya.com", "wrongpassword"))
                .expectError(InvalidCredentialsException.class)
                .verify();

        verify(userRepository).findByEmail("admin@crediya.com");
        verify(passwordEncoderRepository).matches("wrongpassword", validUser.getPassword());
        verify(jwtRepository, never()).generateToken(any(User.class));
    }

    @Test
    void loginFailsWhenPasswordValidationThrowsException() {
        when(userRepository.findByEmail("admin@crediya.com")).thenReturn(Mono.just(validUser));
        when(passwordEncoderRepository.matches("admin123456", validUser.getPassword()))
                .thenReturn(Mono.error(new RuntimeException("Password validation error")));

        StepVerifier.create(authUseCase.login("admin@crediya.com", "admin123456"))
                .expectError(InvalidCredentialsException.class)
                .verify();

        verify(userRepository).findByEmail("admin@crediya.com");
        verify(passwordEncoderRepository).matches("admin123456", validUser.getPassword());
        verify(jwtRepository, never()).generateToken(any(User.class));
    }

    @Test
    void loginFailsWhenJwtGenerationFails() {
        when(userRepository.findByEmail("admin@crediya.com")).thenReturn(Mono.just(validUser));
        when(passwordEncoderRepository.matches("admin123456", validUser.getPassword())).thenReturn(Mono.just(true));
        when(jwtRepository.generateToken(validUser))
                .thenReturn(Mono.error(new RuntimeException("JWT generation failed")));

        StepVerifier.create(authUseCase.login("admin@crediya.com", "admin123456"))
                .expectError(InvalidCredentialsException.class)
                .verify();

        verify(userRepository).findByEmail("admin@crediya.com");
        verify(passwordEncoderRepository).matches("admin123456", validUser.getPassword());
        verify(jwtRepository).generateToken(validUser);
    }

    @Test
    void loginFailsWhenUserRepositoryThrowsException() {
        when(userRepository.findByEmail("admin@crediya.com"))
                .thenReturn(Mono.error(new RuntimeException("Database connection error")));

        StepVerifier.create(authUseCase.login("admin@crediya.com", "admin123456"))
                .expectError(InvalidCredentialsException.class)
                .verify();

        verify(userRepository).findByEmail("admin@crediya.com");
        verify(passwordEncoderRepository, never()).matches(anyString(), anyString());
        verify(jwtRepository, never()).generateToken(any(User.class));
    }

    @Test
    void loginWithDifferentUserRoles() {
        Role asesorRole = Role.builder()
                .id(2L)
                .name("ASESOR")
                .description("Asesor role")
                .build();

        User asesorUser = validUser.toBuilder()
                .role(asesorRole)
                .email("asesor@crediya.com")
                .build();

        JwtToken asesorToken = validToken.toBuilder()
                .userRole("ASESOR")
                .build();

        when(userRepository.findByEmail("asesor@crediya.com")).thenReturn(Mono.just(asesorUser));
        when(passwordEncoderRepository.matches("asesor123456", asesorUser.getPassword())).thenReturn(Mono.just(true));
        when(jwtRepository.generateToken(asesorUser)).thenReturn(Mono.just(asesorToken));

        StepVerifier.create(authUseCase.login("asesor@crediya.com", "asesor123456"))
                .expectNextMatches(token -> token.getUserRole().equals("ASESOR"))
                .verifyComplete();

        verify(userRepository).findByEmail("asesor@crediya.com");
        verify(passwordEncoderRepository).matches("asesor123456", asesorUser.getPassword());
        verify(jwtRepository).generateToken(asesorUser);
    }

    @Test
    void loginHandlesInvalidCredentialsExceptionCorrectly() {
        when(userRepository.findByEmail("admin@crediya.com")).thenReturn(Mono.just(validUser));
        when(passwordEncoderRepository.matches("wrongpassword", validUser.getPassword()))
                .thenReturn(Mono.error(new InvalidCredentialsException("Invalid password")));

        StepVerifier.create(authUseCase.login("admin@crediya.com", "wrongpassword"))
                .expectErrorMatches(throwable -> 
                    throwable instanceof InvalidCredentialsException &&
                    throwable.getMessage().equals("Invalid password"))
                .verify();

        verify(userRepository).findByEmail("admin@crediya.com");
        verify(passwordEncoderRepository).matches("wrongpassword", validUser.getPassword());
        verify(jwtRepository, never()).generateToken(any(User.class));
    }
}
