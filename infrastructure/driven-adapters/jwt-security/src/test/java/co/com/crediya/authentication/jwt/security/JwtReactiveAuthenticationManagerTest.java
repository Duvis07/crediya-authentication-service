package co.com.crediya.authentication.jwt.security;

import co.com.crediya.authentication.model.auth.gateways.JwtRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtReactiveAuthenticationManagerTest {

    private JwtRepository jwtRepository;
    private JwtReactiveAuthenticationManager authenticationManager;
    private String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.valid.token";
    private String invalidToken = "invalid.jwt.token";

    @BeforeEach
    void setUp() {
        jwtRepository = Mockito.mock(JwtRepository.class);
        authenticationManager = new JwtReactiveAuthenticationManager(jwtRepository);
    }

    @Test
    void authenticateWithValidToken() {
        // Arrange
        Authentication inputAuth = new UsernamePasswordAuthenticationToken(null, validToken);
        
        when(jwtRepository.validateToken(validToken)).thenReturn(Mono.just(true));
        when(jwtRepository.extractEmail(validToken)).thenReturn("admin@crediya.com");
        when(jwtRepository.extractRole(validToken)).thenReturn("ADMIN");

        // Act & Assert
        StepVerifier.create(authenticationManager.authenticate(inputAuth))
                .expectNextMatches(authentication -> {
                    assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
                    assertThat(authentication.getPrincipal()).isEqualTo("admin@crediya.com");
                    assertThat(authentication.getCredentials()).isNull();
                    assertThat(authentication.getAuthorities())
                            .hasSize(1)
                            .extracting("authority")
                            .contains("ROLE_ADMIN");
                    assertThat(authentication.isAuthenticated()).isTrue();
                    return true;
                })
                .verifyComplete();

        verify(jwtRepository).validateToken(validToken);
        verify(jwtRepository).extractEmail(validToken);
        verify(jwtRepository).extractRole(validToken);
    }

    @Test
    void authenticateWithInvalidToken() {
        // Arrange
        Authentication inputAuth = new UsernamePasswordAuthenticationToken(null, invalidToken);
        
        when(jwtRepository.validateToken(invalidToken)).thenReturn(Mono.just(false));

        // Act & Assert
        StepVerifier.create(authenticationManager.authenticate(inputAuth))
                .verifyComplete(); // Should complete without emitting any items

        verify(jwtRepository).validateToken(invalidToken);
        verify(jwtRepository, never()).extractEmail(anyString());
        verify(jwtRepository, never()).extractRole(anyString());
    }

    @Test
    void authenticateWithTokenValidationError() {
        // Arrange
        Authentication inputAuth = new UsernamePasswordAuthenticationToken(null, validToken);
        
        when(jwtRepository.validateToken(validToken))
                .thenReturn(Mono.error(new RuntimeException("Token validation failed")));

        // Act & Assert
        StepVerifier.create(authenticationManager.authenticate(inputAuth))
                .expectError(RuntimeException.class)
                .verify();

        verify(jwtRepository).validateToken(validToken);
        verify(jwtRepository, never()).extractEmail(anyString());
        verify(jwtRepository, never()).extractRole(anyString());
    }

    @Test
    void authenticateWithDifferentRoles() {
        // Test ASESOR role
        Authentication inputAuth = new UsernamePasswordAuthenticationToken(null, validToken);
        
        when(jwtRepository.validateToken(validToken)).thenReturn(Mono.just(true));
        when(jwtRepository.extractEmail(validToken)).thenReturn("asesor@crediya.com");
        when(jwtRepository.extractRole(validToken)).thenReturn("ASESOR");

        StepVerifier.create(authenticationManager.authenticate(inputAuth))
                .expectNextMatches(authentication -> {
                    assertThat(authentication.getPrincipal()).isEqualTo("asesor@crediya.com");
                    assertThat(authentication.getAuthorities())
                            .hasSize(1)
                            .extracting("authority")
                            .contains("ROLE_ASESOR");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void authenticateWithApplicantRole() {
        // Test APPLICANT role
        Authentication inputAuth = new UsernamePasswordAuthenticationToken(null, validToken);
        
        when(jwtRepository.validateToken(validToken)).thenReturn(Mono.just(true));
        when(jwtRepository.extractEmail(validToken)).thenReturn("cliente@crediya.com");
        when(jwtRepository.extractRole(validToken)).thenReturn("APPLICANT");

        StepVerifier.create(authenticationManager.authenticate(inputAuth))
                .expectNextMatches(authentication -> {
                    assertThat(authentication.getPrincipal()).isEqualTo("cliente@crediya.com");
                    assertThat(authentication.getAuthorities())
                            .hasSize(1)
                            .extracting("authority")
                            .contains("ROLE_APPLICANT");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void authenticateWithEmailExtractionError() {
        // Arrange
        Authentication inputAuth = new UsernamePasswordAuthenticationToken(null, validToken);
        
        when(jwtRepository.validateToken(validToken)).thenReturn(Mono.just(true));
        when(jwtRepository.extractEmail(validToken))
                .thenThrow(new RuntimeException("Email extraction failed"));

        // Act & Assert
        StepVerifier.create(authenticationManager.authenticate(inputAuth))
                .expectError(RuntimeException.class)
                .verify();

        verify(jwtRepository).validateToken(validToken);
        verify(jwtRepository).extractEmail(validToken);
    }

    @Test
    void authenticateWithRoleExtractionError() {
        // Arrange
        Authentication inputAuth = new UsernamePasswordAuthenticationToken(null, validToken);
        
        when(jwtRepository.validateToken(validToken)).thenReturn(Mono.just(true));
        when(jwtRepository.extractEmail(validToken)).thenReturn("admin@crediya.com");
        when(jwtRepository.extractRole(validToken))
                .thenThrow(new RuntimeException("Role extraction failed"));

        // Act & Assert
        StepVerifier.create(authenticationManager.authenticate(inputAuth))
                .expectError(RuntimeException.class)
                .verify();

        verify(jwtRepository).validateToken(validToken);
        verify(jwtRepository).extractEmail(validToken);
        verify(jwtRepository).extractRole(validToken);
    }

    @Test
    void authenticateWithNullToken() {
        // Arrange
        Authentication inputAuth = new UsernamePasswordAuthenticationToken(null, null);

        // Act & Assert
        StepVerifier.create(authenticationManager.authenticate(inputAuth))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void authenticateWithEmptyToken() {
        // Arrange
        String emptyToken = "";
        Authentication inputAuth = new UsernamePasswordAuthenticationToken(null, emptyToken);
        
        when(jwtRepository.validateToken(emptyToken)).thenReturn(Mono.just(false));

        // Act & Assert
        StepVerifier.create(authenticationManager.authenticate(inputAuth))
                .verifyComplete();

        verify(jwtRepository).validateToken(emptyToken);
    }

    @Test
    void authenticateCreatesCorrectAuthorities() {
        // Arrange
        Authentication inputAuth = new UsernamePasswordAuthenticationToken(null, validToken);
        
        when(jwtRepository.validateToken(validToken)).thenReturn(Mono.just(true));
        when(jwtRepository.extractEmail(validToken)).thenReturn("test@crediya.com");
        when(jwtRepository.extractRole(validToken)).thenReturn("CUSTOM_ROLE");

        // Act & Assert
        StepVerifier.create(authenticationManager.authenticate(inputAuth))
                .expectNextMatches(authentication -> {
                    List<SimpleGrantedAuthority> expectedAuthorities = List.of(
                            new SimpleGrantedAuthority("ROLE_CUSTOM_ROLE")
                    );
                    assertThat(authentication.getAuthorities()).isEqualTo(expectedAuthorities);
                    return true;
                })
                .verifyComplete();
    }
}
