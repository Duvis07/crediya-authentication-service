package co.com.crediya.authentication.api.handler;

import co.com.crediya.authentication.api.dto.LoginRequest;
import co.com.crediya.authentication.api.dto.LoginResponse;
import co.com.crediya.authentication.api.exceptions.ValidationException;
import co.com.crediya.authentication.api.mapper.AuthMapper;
import co.com.crediya.authentication.model.auth.JwtToken;
import co.com.crediya.authentication.model.exceptions.InvalidCredentialsException;
import co.com.crediya.authentication.model.exceptions.UserNotFoundException;
import co.com.crediya.authentication.usecase.AuthUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthHandler Tests")
class AuthHandlerTest {

    @Mock
    private AuthUseCase authUseCase;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private Validator validator;

    private AuthHandler authHandler;

    private LoginRequest validLoginRequest;
    private LoginResponse validLoginResponse;
    private JwtToken validJwtToken;

    @BeforeEach
    void setUp() {
        authHandler = new AuthHandler(authUseCase, authMapper, validator);
        setupTestData();
    }

    private void setupTestData() {
        validLoginRequest = LoginRequest.builder()
                .email("juan.perez@example.com")
                .password("password123")
                .build();

        validJwtToken = JwtToken.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(1L)
                .userRole("APPLICANT")
                .build();

        validLoginResponse = LoginResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userRole("APPLICANT")
                .build();
    }

    @Test
    @DisplayName("login - Should authenticate user successfully when valid credentials")
    void login_ShouldAuthenticateUserSuccessfully_WhenValidCredentials() {
        // Arrange
        ServerRequest request = MockServerRequest.builder()
                .body(Mono.just(validLoginRequest));

        doNothing().when(validator).validate(any(), any(Errors.class));
        when(authUseCase.login(validLoginRequest.getEmail(), validLoginRequest.getPassword()))
                .thenReturn(Mono.just(validJwtToken));
        when(authMapper.toLoginResponse(validJwtToken)).thenReturn(validLoginResponse);

        // Act
        Mono<ServerResponse> result = authHandler.login(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.statusCode().value()).isEqualTo(200);
                })
                .verifyComplete();

        verify(validator).validate(eq(validLoginRequest), any(Errors.class));
        verify(authUseCase).login(validLoginRequest.getEmail(), validLoginRequest.getPassword());
        verify(authMapper).toLoginResponse(validJwtToken);
    }

    @Test
    @DisplayName("login - Should return validation error when invalid request")
    void login_ShouldReturnValidationError_WhenInvalidRequest() {
        // Arrange
        LoginRequest invalidRequest = LoginRequest.builder()
                .email("")
                .password("")
                .build();

        ServerRequest request = MockServerRequest.builder()
                .body(Mono.just(invalidRequest));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("email", "NotBlank", "Email is required");
            errors.rejectValue("password", "NotBlank", "Password is required");
            return null;
        }).when(validator).validate(eq(invalidRequest), any(Errors.class));

        // Act
        Mono<ServerResponse> result = authHandler.login(request);

        // Assert
        StepVerifier.create(result)
                .expectError(ValidationException.class)
                .verify();

        verify(validator).validate(eq(invalidRequest), any(Errors.class));
        verifyNoInteractions(authUseCase, authMapper);
    }

    @Test
    @DisplayName("login - Should handle UserNotFoundException")
    void login_ShouldHandleUserNotFoundException_WhenUserNotFound() {
        // Arrange
        ServerRequest request = MockServerRequest.builder()
                .body(Mono.just(validLoginRequest));

        doNothing().when(validator).validate(any(), any(Errors.class));
        when(authUseCase.login(validLoginRequest.getEmail(), validLoginRequest.getPassword()))
                .thenReturn(Mono.error(new UserNotFoundException("User not found with email: " + validLoginRequest.getEmail())));

        // Act
        Mono<ServerResponse> result = authHandler.login(request);

        // Assert
        StepVerifier.create(result)
                .expectError(UserNotFoundException.class)
                .verify();

        verify(authUseCase).login(validLoginRequest.getEmail(), validLoginRequest.getPassword());
        verifyNoInteractions(authMapper);
    }

    @Test
    @DisplayName("login - Should handle InvalidCredentialsException")
    void login_ShouldHandleInvalidCredentialsException_WhenWrongPassword() {
        // Arrange
        ServerRequest request = MockServerRequest.builder()
                .body(Mono.just(validLoginRequest));

        doNothing().when(validator).validate(any(), any(Errors.class));
        when(authUseCase.login(validLoginRequest.getEmail(), validLoginRequest.getPassword()))
                .thenReturn(Mono.error(new InvalidCredentialsException("Invalid credentials")));

        // Act
        Mono<ServerResponse> result = authHandler.login(request);

        // Assert
        StepVerifier.create(result)
                .expectError(InvalidCredentialsException.class)
                .verify();

        verify(authUseCase).login(validLoginRequest.getEmail(), validLoginRequest.getPassword());
        verifyNoInteractions(authMapper);
    }

    @Test
    @DisplayName("login - Should validate request content type")
    void login_ShouldValidateRequestContentType() {
        // Arrange
        ServerRequest request = MockServerRequest.builder()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(validLoginRequest));

        doNothing().when(validator).validate(any(), any(Errors.class));
        when(authUseCase.login(validLoginRequest.getEmail(), validLoginRequest.getPassword()))
                .thenReturn(Mono.just(validJwtToken));
        when(authMapper.toLoginResponse(validJwtToken)).thenReturn(validLoginResponse);

        // Act
        Mono<ServerResponse> result = authHandler.login(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.statusCode().value()).isEqualTo(200);
                    assertThat(response.headers().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("login - Should handle malformed JSON request")
    void login_ShouldHandleMalformedJsonRequest() {
        // Arrange
        ServerRequest request = MockServerRequest.builder()
                .body(Mono.error(new RuntimeException("Malformed JSON")));

        // Act
        Mono<ServerResponse> result = authHandler.login(request);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verifyNoInteractions(validator, authUseCase, authMapper);
    }
}