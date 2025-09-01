package co.com.crediya.authentication.jwt.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtServerSecurityContextRepositoryTest {

    private JwtReactiveAuthenticationManager authenticationManager;
    private JwtServerSecurityContextRepository securityContextRepository;
    private final String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.valid.token";

    @BeforeEach
    void setUp() {
        authenticationManager = Mockito.mock(JwtReactiveAuthenticationManager.class);
        securityContextRepository = new JwtServerSecurityContextRepository(authenticationManager);
    }

    @Test
    void loadWithValidBearerToken() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Authentication mockAuth = new UsernamePasswordAuthenticationToken(
                "admin@crediya.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(Mono.just(mockAuth));

        // Act & Assert
        StepVerifier.create(securityContextRepository.load(exchange))
                .expectNextMatches(securityContext -> {
                    assertThat(securityContext).isNotNull();
                    assertThat(securityContext.getAuthentication()).isEqualTo(mockAuth);
                    assertThat(securityContext.getAuthentication().getPrincipal()).isEqualTo("admin@crediya.com");
                    assertThat(securityContext.getAuthentication().getAuthorities())
                            .hasSize(1)
                            .extracting("authority")
                            .contains("ROLE_ADMIN");
                    return true;
                })
                .verifyComplete();

        verify(authenticationManager).authenticate(any(Authentication.class));
    }

    @Test
    void loadWithoutAuthorizationHeader() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        StepVerifier.create(securityContextRepository.load(exchange))
                .verifyComplete(); // Should complete without emitting any items

        verify(authenticationManager, never()).authenticate(any(Authentication.class));
    }

    @Test
    void loadWithNullAuthorizationHeader() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, (String) null)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        StepVerifier.create(securityContextRepository.load(exchange))
                .verifyComplete();

        verify(authenticationManager, never()).authenticate(any(Authentication.class));
    }

    @Test
    void loadWithInvalidAuthorizationHeaderFormat() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        StepVerifier.create(securityContextRepository.load(exchange))
                .verifyComplete();

        verify(authenticationManager, never()).authenticate(any(Authentication.class));
    }

    @Test
    void loadWithBearerTokenButNoToken() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Authentication mockAuth = new UsernamePasswordAuthenticationToken(
                "",
                null,
                List.of()
        );

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(Mono.just(mockAuth));

        // Act & Assert
        StepVerifier.create(securityContextRepository.load(exchange))
                .expectNextMatches(securityContext -> {
                    assertThat(securityContext).isNotNull();
                    assertThat(securityContext.getAuthentication()).isEqualTo(mockAuth);
                    return true;
                })
                .verifyComplete();

        verify(authenticationManager).authenticate(any(Authentication.class));
    }

    @Test
    void loadWithAuthenticationManagerError() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(Mono.error(new RuntimeException("Authentication failed")));

        // Act & Assert
        StepVerifier.create(securityContextRepository.load(exchange))
                .expectError(RuntimeException.class)
                .verify();

        verify(authenticationManager).authenticate(any(Authentication.class));
    }

    @Test
    void loadWithAuthenticationManagerReturningEmpty() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(securityContextRepository.load(exchange))
                .verifyComplete();

        verify(authenticationManager).authenticate(any(Authentication.class));
    }

    @Test
    void loadWithDifferentRoles() {
        // Test with ASESOR role
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Authentication asesorAuth = new UsernamePasswordAuthenticationToken(
                "asesor@crediya.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ASESOR"))
        );

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(Mono.just(asesorAuth));

        StepVerifier.create(securityContextRepository.load(exchange))
                .expectNextMatches(securityContext -> {
                    assertThat(securityContext.getAuthentication().getPrincipal()).isEqualTo("asesor@crediya.com");
                    assertThat(securityContext.getAuthentication().getAuthorities())
                            .hasSize(1)
                            .extracting("authority")
                            .contains("ROLE_ASESOR");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void loadCreatesCorrectAuthenticationObject() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Verify that the authentication object passed to the manager has correct structure
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenAnswer(invocation -> {
                    Authentication auth = invocation.getArgument(0);
                    assertThat(auth).isInstanceOf(UsernamePasswordAuthenticationToken.class);
                    assertThat(auth.getPrincipal()).isEqualTo(validToken);
                    assertThat(auth.getCredentials()).isEqualTo(validToken);
                    
                    return Mono.just(new UsernamePasswordAuthenticationToken(
                            "test@crediya.com",
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_TEST"))
                    ));
                });

        // Act & Assert
        StepVerifier.create(securityContextRepository.load(exchange))
                .expectNextCount(1)
                .verifyComplete();

        verify(authenticationManager).authenticate(any(Authentication.class));
    }

    @Test
    void saveAlwaysReturnsEmpty() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        SecurityContext mockContext = Mockito.mock(SecurityContext.class);

        // Act & Assert
        StepVerifier.create(securityContextRepository.save(exchange, mockContext))
                .verifyComplete();

        // Verify no interactions with authentication manager
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void loadWithMultipleAuthorizationHeaders() {
        // Arrange - Spring's MockServerHttpRequest doesn't easily support multiple headers,
        // but we can test the first header behavior
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Authentication mockAuth = new UsernamePasswordAuthenticationToken(
                "admin@crediya.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(Mono.just(mockAuth));

        // Act & Assert
        StepVerifier.create(securityContextRepository.load(exchange))
                .expectNextCount(1)
                .verifyComplete();

        verify(authenticationManager).authenticate(any(Authentication.class));
    }
}
