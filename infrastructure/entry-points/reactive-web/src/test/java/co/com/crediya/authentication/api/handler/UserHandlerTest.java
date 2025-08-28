package co.com.crediya.authentication.api.handler;

import co.com.crediya.authentication.api.dto.CreateUserRequest;
import co.com.crediya.authentication.api.dto.UserResponse;
import co.com.crediya.authentication.api.dto.RoleResponse;
import co.com.crediya.authentication.api.exceptions.ValidationException;
import co.com.crediya.authentication.api.mapper.UserMapper;
import co.com.crediya.authentication.model.exceptions.InvalidUserDataException;
import co.com.crediya.authentication.model.exceptions.UserAlreadyExistsException;
import co.com.crediya.authentication.model.role.Role;
import co.com.crediya.authentication.model.user.User;
import co.com.crediya.authentication.model.user.UserType;
import co.com.crediya.authentication.usecase.UserUseCase;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserHandler Tests")
class UserHandlerTest {

    @Mock
    private UserUseCase userUseCase;

    @Mock
    private UserMapper userMapper;

    @Mock
    private Validator validator;

    private UserHandler userHandler;

    private CreateUserRequest validCreateUserRequest;
    private User validUser;
    private UserResponse validUserResponse;

    @BeforeEach
    void setUp() {
        userHandler = new UserHandler(userUseCase, userMapper, validator);
        setupTestData();
    }

    private void setupTestData() {
        validCreateUserRequest = CreateUserRequest.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .birthDate(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67")
                .phone("+573001234567")
                .email("juan.perez@example.com")
                .password("password123")
                .baseSalary(new BigDecimal("3000000"))
                .userType("APPLICANT")
                .build();

        Role role = Role.builder()
                .id(1L)
                .name("Solicitante")
                .description("Usuario solicitante de crédito")
                .build();

        validUser = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .birthDate(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67")
                .phone("+573001234567")
                .email("juan.perez@example.com")
                .password("hashedPassword")
                .baseSalary(new BigDecimal("3000000"))
                .role(role)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        RoleResponse roleResponse = RoleResponse.builder()
                .id(1L)
                .name("Solicitante")
                .description("Usuario solicitante de crédito")
                .build();

        validUserResponse = UserResponse.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .birthDate(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67")
                .phone("+573001234567")
                .email("juan.perez@example.com")
                .baseSalary(new BigDecimal("3000000"))
                .role(roleResponse)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("createUser - Should create user successfully when valid request")
    void createUser_ShouldCreateUserSuccessfully_WhenValidRequest() {
        // Arrange
        ServerRequest request = MockServerRequest.builder()
                .body(Mono.just(validCreateUserRequest));

        doNothing().when(validator).validate(any(), any(Errors.class));
        when(userMapper.toUser(validCreateUserRequest)).thenReturn(validUser);
        when(userMapper.mapUserType(validCreateUserRequest)).thenReturn(UserType.APPLICANT);
        when(userUseCase.createUser(validUser, UserType.APPLICANT)).thenReturn(Mono.just(validUser));

        // Act
        Mono<ServerResponse> result = userHandler.createUser(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.statusCode().value()).isEqualTo(200);
                })
                .verifyComplete();

        verify(validator).validate(eq(validCreateUserRequest), any(Errors.class));
        verify(userMapper).toUser(validCreateUserRequest);
        verify(userMapper).mapUserType(validCreateUserRequest);
        verify(userUseCase).createUser(validUser, UserType.APPLICANT);
    }

    @Test
    @DisplayName("createUser - Should return validation error when invalid request")
    void createUser_ShouldReturnValidationError_WhenInvalidRequest() {
        // Arrange
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                .firstName("")
                .email("invalid-email")
                .build();

        ServerRequest request = MockServerRequest.builder()
                .body(Mono.just(invalidRequest));

        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("firstName", "NotBlank", "First name is required");
            errors.rejectValue("email", "Email", "Invalid email format");
            return null;
        }).when(validator).validate(eq(invalidRequest), any(Errors.class));

        // Act
        Mono<ServerResponse> result = userHandler.createUser(request);

        // Assert
        StepVerifier.create(result)
                .expectError(ValidationException.class)
                .verify();

        verify(validator).validate(eq(invalidRequest), any(Errors.class));
        verifyNoInteractions(userMapper, userUseCase);
    }

    @Test
    @DisplayName("createUser - Should handle UserAlreadyExistsException")
    void createUser_ShouldHandleUserAlreadyExistsException_WhenEmailExists() {
        // Arrange
        ServerRequest request = MockServerRequest.builder()
                .body(Mono.just(validCreateUserRequest));

        doNothing().when(validator).validate(any(), any(Errors.class));
        when(userMapper.toUser(validCreateUserRequest)).thenReturn(validUser);
        when(userMapper.mapUserType(validCreateUserRequest)).thenReturn(UserType.APPLICANT);
        when(userUseCase.createUser(validUser, UserType.APPLICANT))
                .thenReturn(Mono.error(new UserAlreadyExistsException("A user with this email is already registered")));

        // Act
        Mono<ServerResponse> result = userHandler.createUser(request);

        // Assert
        StepVerifier.create(result)
                .expectError(UserAlreadyExistsException.class)
                .verify();

        verify(userUseCase).createUser(validUser, UserType.APPLICANT);
    }

    @Test
    @DisplayName("createUser - Should handle InvalidUserDataException")
    void createUser_ShouldHandleInvalidUserDataException_WhenInvalidUserType() {
        // Arrange
        ServerRequest request = MockServerRequest.builder()
                .body(Mono.just(validCreateUserRequest));

        doNothing().when(validator).validate(any(), any(Errors.class));
        when(userMapper.toUser(validCreateUserRequest)).thenReturn(validUser);
        when(userMapper.mapUserType(validCreateUserRequest))
                .thenThrow(new InvalidUserDataException("Invalid user type: INVALID"));

        // Act
        Mono<ServerResponse> result = userHandler.createUser(request);

        // Assert
        StepVerifier.create(result)
                .expectError(InvalidUserDataException.class)
                .verify();

        verify(userMapper).mapUserType(validCreateUserRequest);
        verifyNoInteractions(userUseCase);
    }

    @Test
    @DisplayName("getAllUsers - Should return all users successfully")
    void getAllUsers_ShouldReturnAllUsersSuccessfully() {
        // Arrange
        ServerRequest request = MockServerRequest.builder().build();

        User user1 = validUser;
        User user2 = validUser.toBuilder()
                .id(2L)
                .email("maria.garcia@example.com")
                .firstName("María")
                .lastName("García")
                .build();

        UserResponse userResponse1 = validUserResponse;
        UserResponse userResponse2 = validUserResponse.toBuilder()
                .id(2L)
                .email("maria.garcia@example.com")
                .firstName("María")
                .lastName("García")
                .build();

        when(userUseCase.findAllUsers()).thenReturn(Flux.just(user1, user2));
        when(userMapper.toUserResponse(user1)).thenReturn(userResponse1);
        when(userMapper.toUserResponse(user2)).thenReturn(userResponse2);

        // Act
        Mono<ServerResponse> result = userHandler.getAllUsers(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.statusCode().value()).isEqualTo(200);
                })
                .verifyComplete();

        verify(userUseCase).findAllUsers();
        verify(userMapper).toUserResponse(user1);
        verify(userMapper).toUserResponse(user2);
    }

    @Test
    @DisplayName("getAllUsers - Should return empty list when no users exist")
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsersExist() {
        // Arrange
        ServerRequest request = MockServerRequest.builder().build();

        when(userUseCase.findAllUsers()).thenReturn(Flux.empty());

        // Act
        Mono<ServerResponse> result = userHandler.getAllUsers(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.statusCode().value()).isEqualTo(200);
                })
                .verifyComplete();

        verify(userUseCase).findAllUsers();
        verifyNoMoreInteractions(userMapper);
    }

    @Test
    @DisplayName("getAllUsers - Should handle error from use case")
    void getAllUsers_ShouldHandleError_WhenUseCaseThrowsException() {
        // Arrange
        ServerRequest request = MockServerRequest.builder().build();

        when(userUseCase.findAllUsers())
                .thenReturn(Flux.error(new RuntimeException("Database connection error")));

        // Act
        Mono<ServerResponse> result = userHandler.getAllUsers(request);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(userUseCase).findAllUsers();
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("createUser - Should validate request content type")
    void createUser_ShouldValidateRequestContentType() {
        // Arrange
        ServerRequest request = MockServerRequest.builder()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(validCreateUserRequest));

        doNothing().when(validator).validate(any(), any(Errors.class));
        when(userMapper.toUser(validCreateUserRequest)).thenReturn(validUser);
        when(userMapper.mapUserType(validCreateUserRequest)).thenReturn(UserType.APPLICANT);
        when(userUseCase.createUser(validUser, UserType.APPLICANT)).thenReturn(Mono.just(validUser));

        // Act
        Mono<ServerResponse> result = userHandler.createUser(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.statusCode().value()).isEqualTo(200);
                    assertThat(response.headers().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
                })
                .verifyComplete();
    }
}
