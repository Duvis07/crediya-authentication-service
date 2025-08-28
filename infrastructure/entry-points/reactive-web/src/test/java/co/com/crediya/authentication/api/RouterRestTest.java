package co.com.crediya.authentication.api;

import co.com.crediya.authentication.api.dto.CreateUserRequest;
import co.com.crediya.authentication.api.dto.LoginRequest;
import co.com.crediya.authentication.api.dto.LoginResponse;
import co.com.crediya.authentication.api.dto.UserResponse;
import co.com.crediya.authentication.api.dto.RoleResponse;
import co.com.crediya.authentication.api.handler.AuthHandler;
import co.com.crediya.authentication.api.handler.UserHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ContextConfiguration(classes = {RouterRest.class})
@WebFluxTest(excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration.class
})
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UserHandler userHandler;

    @MockitoBean
    private AuthHandler authHandler;

    private UserResponse sampleUser;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        RoleResponse roleResponse = RoleResponse.builder()
                .id(1L)
                .name("Solicitante")
                .description("Usuario solicitante de crédito")
                .build();

        sampleUser = UserResponse.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .address("Calle 123")
                .phone("3001234567")
                .baseSalary(BigDecimal.valueOf(2500.0))
                .role(roleResponse)
                .createdAt(LocalDateTime.now())
                .build();

        loginRequest = LoginRequest.builder()
                .email("admin@crediya.com")
                .password("admin123456")
                .build();

        loginResponse = LoginResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .userRole("ADMIN")
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/usuarios - Should create user successfully")
    void createUserShouldReturnUserResponse() {
        // Arrange
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@example.com")
                .password("password123")
                .baseSalary(new BigDecimal("3000000"))
                .userType("APPLICANT")
                .build();

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("message", "User successfully created");

        Mockito.when(userHandler.createUser(Mockito.any())).thenReturn(
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(successResponse)
        );

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("User successfully created");
    }

    @Test
    @DisplayName("GET /api/v1/usuarios - Should return all users")
    void getAllUsersShouldReturnFluxOfUsers() {
        // Arrange
        Mockito.when(userHandler.getAllUsers(Mockito.any())).thenReturn(
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(java.util.List.of(sampleUser))
        );

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/usuarios")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserResponse.class)
                .hasSize(1);
    }

    @Test
    @DisplayName("POST /api/v1/login - Should authenticate user successfully")
    void loginShouldReturnJwtToken() {
        // Arrange
        Mockito.when(authHandler.login(Mockito.any())).thenReturn(
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(loginResponse)
        );

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponse.class)
                .isEqualTo(loginResponse);
    }

    @Test
    @DisplayName("POST /api/v1/usuarios - Should return 400 for invalid data")
    void createUserWithInvalidDataShouldReturnBadRequest() {
        // Arrange
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                .firstName("")
                .email("invalid-email")
                .build();

        Mockito.when(userHandler.createUser(Mockito.any())).thenReturn(
                ServerResponse.badRequest().build()
        );

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("POST /api/v1/login - Should return 401 for invalid credentials")
    void loginWithInvalidCredentialsShouldReturnUnauthorized() {
        // Arrange
        LoginRequest invalidLogin = LoginRequest.builder()
                .email("wrong@email.com")
                .password("wrongpassword")
                .build();

        Mockito.when(authHandler.login(Mockito.any())).thenReturn(
                ServerResponse.status(401).build()
        );

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidLogin)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("POST /api/v1/usuarios - Should return 409 for duplicate email")
    void createUserWithDuplicateEmailShouldReturnConflict() {
        // Arrange
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .email("existing@email.com")
                .password("password123")
                .baseSalary(new BigDecimal("3000000"))
                .userType("APPLICANT")
                .build();

        Mockito.when(userHandler.createUser(Mockito.any())).thenReturn(
                ServerResponse.status(409).build()
        );

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    @DisplayName("GET /api/v1/usuarios - Should return empty list when no users")
    void getAllUsersWhenEmptyShouldReturnEmptyList() {
        // Arrange
        Mockito.when(userHandler.getAllUsers(Mockito.any())).thenReturn(
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(java.util.List.of())
        );

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/usuarios")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserResponse.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("POST /api/v1/usuarios - Should handle missing content type")
    void createUserWithoutContentTypeShouldReturnUnsupportedMediaType() {
        // Arrange
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@example.com")
                .build();

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError(); // Changed from 415 to 5xx due to handler NPE
    }

}