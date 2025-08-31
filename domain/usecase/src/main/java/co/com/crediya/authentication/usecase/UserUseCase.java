package co.com.crediya.authentication.usecase;

import co.com.crediya.authentication.model.exceptions.DuplicateDocumentException;
import co.com.crediya.authentication.model.exceptions.InvalidUserDataException;
import co.com.crediya.authentication.model.exceptions.UserAlreadyExistsException;
import co.com.crediya.authentication.model.user.User;
import co.com.crediya.authentication.model.user.UserType;
import co.com.crediya.authentication.model.user.gateways.UserRepository;
import co.com.crediya.authentication.model.role.gateways.RoleRepository;
import co.com.crediya.authentication.model.utils.UserValidator;
import co.com.crediya.authentication.model.auth.gateways.PasswordEncoderRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.logging.Level;

@RequiredArgsConstructor
public class UserUseCase {

    private static final Logger log = Logger.getLogger(UserUseCase.class.getName());

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoderRepository passwordEncoderRepository;

    public Mono<User> createUser(User user, UserType userType) {
        log.log(Level.INFO, "Creating user with email: {0} and type: {1}", new Object[]{user.getEmail(), userType});

        return UserValidator.validateUserData(user)
                .then(checkEmailUniqueness(user.getEmail()))
                .then(encryptPasswordAndAssignRole(user, userType))
                .flatMap(userRepository::save)
                .doOnSuccess(saved -> log.log(Level.INFO, "User created with ID: {0}", saved.getId()))
                .doOnError(error -> log.log(Level.SEVERE, "Error creating user: {0}", error.getMessage()))
                .onErrorMap(ex -> ex.getClass().getName().contains("DuplicateKeyException"),
                    ex -> new DuplicateDocumentException("Document ID already exists"));
    }

    public Flux<User> findAllUsers() {
        log.fine("Finding all users");
        return userRepository.findAll();
    }

    public Mono<User> findByDocumentId(String documentId) {
        log.log(Level.INFO, "Finding user by documentId: {0}", documentId);
        return userRepository.findByDocumentId(documentId)
                .doOnSuccess(user -> log.log(Level.INFO, "User found with documentId: {0}", documentId))
                .doOnError(error -> log.log(Level.SEVERE, "Error finding user by documentId {0}: {1}",
                        new Object[]{documentId, error.getMessage()}));
    }

    // PRIVATE METHODS

    private Mono<Void> checkEmailUniqueness(String email) {
        return userRepository.existsByEmail(email)
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new UserAlreadyExistsException(
                        "A user with this email is already registered")))
                .then();
    }

    private Mono<User> encryptPasswordAndAssignRole(User user, UserType userType) {
        return passwordEncoderRepository.encode(user.getPassword())
                .flatMap(encryptedPassword -> {
                    user.setPassword(encryptedPassword);
                    log.log(Level.INFO, "Password encrypted for user: {0}", user.getEmail());
                    return assignRoleToUser(user, userType);
                });
    }

    private Mono<User> assignRoleToUser(User user, UserType userType) {
        return roleRepository.findByCode(userType.getCode())
                .switchIfEmpty(Mono.error(new InvalidUserDataException(
                        "Role not found for user type: " + userType.getCode())))
                .map(role -> {
                    user.setRole(role);
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());
                    return user;
                });
    }
}