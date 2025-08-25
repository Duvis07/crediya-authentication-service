package co.com.crediya.authentication.usecase;

import co.com.crediya.authentication.model.exceptions.InvalidUserDataException;
import co.com.crediya.authentication.model.exceptions.UserAlreadyExistsException;
import co.com.crediya.authentication.model.exceptions.UserNotFoundException;
import co.com.crediya.authentication.model.role.Role;
import co.com.crediya.authentication.model.role.gateways.RoleRepository;
import co.com.crediya.authentication.model.user.User;
import co.com.crediya.authentication.model.user.UserType;
import co.com.crediya.authentication.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
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
        userUseCase = new UserUseCase(userRepository, roleRepository);

        validUser = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@example.com")
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
        when(roleRepository.findByCode("Solicitante")).thenReturn(Mono.just(defaultRole));
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
        when(roleRepository.findByCode("Solicitante")).thenReturn(Mono.just(defaultRole));

        StepVerifier.create(userUseCase.createUser(validUser, UserType.APPLICANT))
                .expectError(UserAlreadyExistsException.class)
                .verify();
    }


    @Test
    void createUserFailsWhenRoleNotFound() {
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(false));
        when(roleRepository.findByCode("Solicitante")).thenReturn(Mono.empty());

        StepVerifier.create(userUseCase.createUser(validUser, UserType.APPLICANT))
                .expectError(InvalidUserDataException.class)
                .verify();
    }

    @Test
    void findUserByIdSuccess() {
        when(userRepository.findById(1L)).thenReturn(Mono.just(validUser));

        StepVerifier.create(userUseCase.findUserById(1L))
                .expectNext(validUser)
                .verifyComplete();
    }

    @Test
    void findUserByIdNotFound() {
        when(userRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userUseCase.findUserById(1L))
                .expectError(UserNotFoundException.class)
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
    void updateUserSuccess() {
        User updated = validUser.toBuilder().firstName("Carlos").build();

        when(userRepository.findById(1L)).thenReturn(Mono.just(validUser));
        when(userRepository.findByEmail(updated.getEmail())).thenReturn(Mono.just(updated));
        when(userRepository.update(any(User.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(userUseCase.updateUser(1L, updated))
                .expectNextMatches(user -> user.getFirstName().equals("Carlos"))
                .verifyComplete();
    }

    @Test
    void updateUserFailsWhenUserNotFound() {
        User updated = validUser.toBuilder().firstName("Carlos").build();

        when(userRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userUseCase.updateUser(1L, updated))
                .expectError(UserNotFoundException.class)
                .verify();
    }

    @Test
    void deleteUserSuccess() {
        when(userRepository.findById(1L)).thenReturn(Mono.just(validUser));
        when(userRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(userUseCase.deleteUser(1L))
                .verifyComplete();

        verify(userRepository).deleteById(1L);
    }
}
