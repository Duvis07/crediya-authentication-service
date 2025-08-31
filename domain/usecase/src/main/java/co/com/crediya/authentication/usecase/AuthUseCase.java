package co.com.crediya.authentication.usecase;

import co.com.crediya.authentication.model.auth.JwtToken;
import co.com.crediya.authentication.model.auth.gateways.JwtRepository;
import co.com.crediya.authentication.model.auth.gateways.PasswordEncoderRepository;
import co.com.crediya.authentication.model.exceptions.InvalidCredentialsException;
import co.com.crediya.authentication.model.user.User;
import co.com.crediya.authentication.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class AuthUseCase {

    private static final Logger log = Logger.getLogger(AuthUseCase.class.getName());

    private final UserRepository userRepository;
    private final JwtRepository jwtRepository;
    private final PasswordEncoderRepository passwordEncoderRepository;

    public Mono<JwtToken> login(String email, String password) {

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException("Invalid credentials")))
                .flatMap(user -> validatePasswordAndGenerateToken(user, password))
                .onErrorMap(Exception.class, ex -> {
                    if (ex instanceof InvalidCredentialsException) {
                        return ex;
                    }
                    log.severe(String.format("Error during authentication for email: %s - %s", email, ex.getMessage()));
                    return new InvalidCredentialsException("Authentication failed");
                });
    }

    private Mono<JwtToken> validatePasswordAndGenerateToken(User user, String password) {
        return passwordEncoderRepository.matches(password, user.getPassword())
                .filter(isValid -> isValid)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warning(String.format("Invalid password attempt for user: %s", user.getEmail()));
                    return Mono.error(new InvalidCredentialsException("Invalid credentials"));
                }))
                .flatMap(isValid -> {
                    log.info(String.format("Password validated successfully for user: %s", user.getEmail()));
                    return jwtRepository.generateToken(user);
                });
    }
}