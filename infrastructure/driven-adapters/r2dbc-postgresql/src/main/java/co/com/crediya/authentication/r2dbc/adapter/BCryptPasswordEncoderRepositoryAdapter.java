package co.com.crediya.authentication.r2dbc.adapter;

import co.com.crediya.authentication.model.auth.gateways.PasswordEncoderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BCryptPasswordEncoderRepositoryAdapter implements PasswordEncoderRepository {
    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordEncoderRepositoryAdapter() {
        this.encoder = new BCryptPasswordEncoder();
    }

    @Override
    public Mono<Boolean> matches(String rawPassword, String encodedPassword) {
        return Mono.fromCallable(() -> {
                    log.info("DEBUG - Password validation: Raw='{}', Stored='{}'", rawPassword, encodedPassword);

                    // Verificar formato del hash
                    if (!encodedPassword.startsWith("$2a$") && !encodedPassword.startsWith("$2b$") && !encodedPassword.startsWith("$2y$")) {
                        log.error("Invalid BCrypt hash format: {}", encodedPassword);
                        return false;
                    }

                    // Verificar longitud del hash (debe ser 60 caracteres)
                    if (encodedPassword.length() != 60) {
                        log.error("Invalid BCrypt hash length: {} (expected 60)", encodedPassword.length());
                        return false;
                    }

                    try {
                        boolean result = encoder.matches(rawPassword, encodedPassword);
                        log.info("BCrypt validation result: {} for password '{}' against hash '{}'",
                                result, rawPassword, encodedPassword.substring(0, 20) + "...");
                        return result;
                    } catch (Exception e) {
                        log.error("BCrypt validation failed with exception: {}", e.getMessage(), e);
                        return false;
                    }
                })
                .doOnNext(matches -> log.debug("Final password validation result: {}", matches))
                .onErrorReturn(false);
    }

    @Override
    public Mono<String> encode(String rawPassword) {
        return Mono.fromCallable(() -> {
                    String encoded = encoder.encode(rawPassword);
                    log.info("DEBUG - Generated hash for '{}': '{}'", rawPassword, encoded);
                    return encoded;
                })
                .doOnNext(encoded -> log.debug("Password encoded successfully"))
                .onErrorMap(Exception.class, ex -> {
                    log.error("Error encoding password", ex);
                    return new RuntimeException("Failed to encode password", ex);
                });
    }
}
