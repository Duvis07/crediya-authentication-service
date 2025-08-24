package co.com.crediya.authentication.usecase;

import co.com.crediya.authentication.model.exceptions.InvalidUserDataException;
import co.com.crediya.authentication.model.exceptions.UserAlreadyExistsException;
import co.com.crediya.authentication.model.exceptions.UserNotFoundException;
import co.com.crediya.authentication.model.user.User;
import co.com.crediya.authentication.model.user.gateways.UserRepository;
import co.com.crediya.authentication.model.role.gateways.RoleRepository;
import co.com.crediya.authentication.model.utils.UserValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.logging.Level;

@RequiredArgsConstructor
public class UserUseCase {

    private static final String DEFAULT_ROLE_NAME = "Solicitante";
    private static final Logger log = Logger.getLogger(UserUseCase.class.getName());

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public Mono<User> createUser(User user) {
        log.log(Level.INFO, "Creating user with email: {0}", user.getEmail());

        return UserValidator.validateUserData(user)
                .then(validateEmailNotExists(user.getEmail()))
                .then(assignDefaultRoleToUser(user))
                .flatMap(userRepository::save)
                .doOnSuccess(saved -> log.log(Level.INFO, "User created with ID: {0}", saved.getId()))
                .doOnError(error -> log.log(Level.SEVERE, "Error creating user: {0}", error.getMessage()));
    }

    public Mono<User> findUserById(Long id) {
        log.log(Level.FINE, "Finding user with ID: {0}", id);
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with ID: " + id)));
    }

    public Flux<User> findAllUsers() {
        log.fine("Finding all users");
        return userRepository.findAll();
    }

    public Mono<User> updateUser(Long id, User userUpdate) {
        log.log(Level.INFO, "Updating user with ID: {0}", id);

        return UserValidator.validateUserData(userUpdate)
                .then(findUserById(id))
                .flatMap(existing -> validateEmailForUpdate(userUpdate.getEmail(), id)
                        .thenReturn(existing.toBuilder()
                                .firstName(userUpdate.getFirstName())
                                .lastName(userUpdate.getLastName())
                                .email(userUpdate.getEmail())
                                .birthDate(userUpdate.getBirthDate())
                                .address(userUpdate.getAddress())
                                .phone(userUpdate.getPhone())
                                .baseSalary(userUpdate.getBaseSalary())
                                .updatedAt(LocalDateTime.now())
                                .build()))
                .flatMap(userRepository::update)
                .doOnSuccess(updated -> log.log(Level.INFO, "User updated with ID: {0}", updated.getId()));
    }

    public Mono<Void> deleteUser(Long id) {
        log.log(Level.INFO, "Deleting user with ID: {0}", id);
        return findUserById(id)
                .flatMap(user -> userRepository.deleteById(id))
                .doOnSuccess(unused -> log.log(Level.INFO, "User deleted with ID: {0}", id));
    }

    // PRIVATE METHODS - Pure domain logic

    private Mono<Void> validateEmailNotExists(String email) {
        return userRepository.existsByEmail(email)
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new UserAlreadyExistsException(
                        "A user with this email is already registered")))
                .then();
    }

    private Mono<Void> validateEmailForUpdate(String email, Long userId) {
        return userRepository.findByEmail(email)
                .filter(existing -> existing.getId().equals(userId))
                .switchIfEmpty(Mono.error(new UserAlreadyExistsException(
                        "A user with this email is already registered")))
                .then();
    }

    private Mono<User> assignDefaultRoleToUser(User user) {
        return roleRepository.findByName(DEFAULT_ROLE_NAME)
                .switchIfEmpty(Mono.error(new InvalidUserDataException(
                        "Default role 'Solicitante' not found")))
                .map(role -> {
                    LocalDateTime now = LocalDateTime.now();
                    return user.toBuilder()
                            .role(role)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                });
    }
}