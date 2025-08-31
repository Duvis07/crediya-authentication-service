package co.com.crediya.authentication.usecase;

import co.com.crediya.authentication.model.exceptions.InvalidUserDataException;
import co.com.crediya.authentication.model.exceptions.UserAlreadyExistsException;
import co.com.crediya.authentication.model.role.Role;
import co.com.crediya.authentication.model.role.gateways.RoleRepository;
import co.com.crediya.authentication.model.user.User;
import co.com.crediya.authentication.model.user.UserType;
import co.com.crediya.authentication.model.user.gateways.UserRepository;
import co.com.crediya.authentication.model.auth.gateways.PasswordEncoderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserUseCaseTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserUseCase userUseCase;

    private User validUser;
    private Role defaultRole;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        roleRepository = Mockito.mock(RoleRepository.class);
        PasswordEncoderRepository passwordEncoder = Mockito.mock(PasswordEncoderRepository.class);
        userUseCase = new UserUseCase(userRepository, roleRepository, passwordEncoder);

        // Mock password encoder to return encrypted password
        when(passwordEncoder.encode(anyString())).thenReturn(Mono.just("$2a$10$encrypted.password.hash"));

        validUser = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@example.com")
                .password("plainPassword123")
                .birthDate(LocalDate.of(1990, 5, 15))
                .baseSalary(BigDecimal.valueOf(5_000_000))
                .address("Calle 123")
                .phone("3001234567")
                .build();

        defaultRole = Role.builder().id(10L).name("Solicitante").build();
    }

    @Test
    void createUserSuccess() {
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(false));
        when(roleRepository.findByCode("APPLICANT")).thenReturn(Mono.just(defaultRole));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(userUseCase.createUser(validUser, UserType.APPLICANT))
                .expectNextMatches(user -> user.getRole().getName().equals("Solicitante"))
                .verifyComplete();

        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserFailsWhenEmailExists() {
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(true));
        when(roleRepository.findByCode("APPLICANT")).thenReturn(Mono.just(defaultRole));

        StepVerifier.create(userUseCase.createUser(validUser, UserType.APPLICANT))
                .expectError(UserAlreadyExistsException.class)
                .verify();
    }


    @Test
    void createUserFailsWhenRoleNotFound() {
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(false));
        when(roleRepository.findByCode("APPLICANT")).thenReturn(Mono.empty());

        StepVerifier.create(userUseCase.createUser(validUser, UserType.APPLICANT))
                .expectError(InvalidUserDataException.class)
                .verify();
    }

    @Test
    void findAllUsers() {
        when(userRepository.findAll()).thenReturn(Flux.just(validUser));

        StepVerifier.create(userUseCase.findAllUsers())
                .expectNext(validUser)
                .verifyComplete();
    }

    @Test
    void createUserWithAdminRole() {
        Role adminRole = Role.builder().id(2L).name("ADMIN").build();

        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(false));
        when(roleRepository.findByCode("ADMIN")).thenReturn(Mono.just(adminRole));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(userUseCase.createUser(validUser, UserType.ADMIN))
                .expectNextMatches(user -> user.getRole().getName().equals("ADMIN"))
                .verifyComplete();

        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserWithAsesorRole() {
        Role asesorRole = Role.builder().id(3L).name("ASESOR").build();

        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(false));
        when(roleRepository.findByCode("ASESOR")).thenReturn(Mono.just(asesorRole));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(userUseCase.createUser(validUser, UserType.ASESOR))
                .expectNextMatches(user -> user.getRole().getName().equals("ASESOR"))
                .verifyComplete();

        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserFailsWhenValidationFails() {
        User invalidUser = User.builder()
                .firstName("")  // Invalid: empty first name
                .lastName("Pérez")
                .email("invalid-email")  // Invalid: bad email format
                .password("password123")
                .baseSalary(BigDecimal.valueOf(-1000))  // Invalid: negative salary
                .build();


        when(userRepository.existsByEmail(invalidUser.getEmail())).thenReturn(Mono.just(false));
        when(roleRepository.findByCode("APPLICANT")).thenReturn(Mono.just(defaultRole));

        StepVerifier.create(userUseCase.createUser(invalidUser, UserType.APPLICANT))
                .expectError(InvalidUserDataException.class)
                .verify();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserFailsWhenRepositorySaveFails() {
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(false));
        when(roleRepository.findByCode("APPLICANT")).thenReturn(Mono.just(defaultRole));
        when(userRepository.save(any(User.class)))
                .thenReturn(Mono.error(new RuntimeException("Database connection failed")));

        StepVerifier.create(userUseCase.createUser(validUser, UserType.APPLICANT))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void createUserSetsTimestampsCorrectly() {
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(false));
        when(roleRepository.findByCode("APPLICANT")).thenReturn(Mono.just(defaultRole));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(userUseCase.createUser(validUser, UserType.APPLICANT))
                .expectNextMatches(user ->
                        user.getCreatedAt() != null &&
                                user.getUpdatedAt() != null &&
                                user.getRole() != null)
                .verifyComplete();
    }

    @Test
    void findAllUsersReturnsEmptyWhenNoUsers() {
        when(userRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(userUseCase.findAllUsers())
                .verifyComplete();
    }

    @Test
    void findAllUsersReturnsMultipleUsers() {
        User user2 = User.builder()
                .id(2L)
                .firstName("Maria")
                .lastName("González")
                .email("maria@example.com")
                .build();

        when(userRepository.findAll()).thenReturn(Flux.just(validUser, user2));

        StepVerifier.create(userUseCase.findAllUsers())
                .expectNext(validUser)
                .expectNext(user2)
                .verifyComplete();
    }

    @Test
    void createUserFailsWhenEmailCheckThrowsException() {
        when(userRepository.existsByEmail(validUser.getEmail()))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(userUseCase.createUser(validUser, UserType.APPLICANT))
                .expectError(RuntimeException.class)
                .verify();

        verify(userRepository, never()).save(any(User.class));
        verify(roleRepository, never()).findByCode(anyString());
    }
}
