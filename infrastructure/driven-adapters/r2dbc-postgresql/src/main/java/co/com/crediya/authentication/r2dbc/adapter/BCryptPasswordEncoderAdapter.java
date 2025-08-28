package co.com.crediya.authentication.r2dbc.adapter;

import co.com.crediya.authentication.model.auth.gateways.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class BCryptPasswordEncoderAdapter implements PasswordEncoder {

    private static final Logger log = LoggerFactory.getLogger(BCryptPasswordEncoderAdapter.class);
    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordEncoderAdapter() {
        this.encoder = new BCryptPasswordEncoder();
    }

    @Override
    public Mono<Boolean> matches(String rawPassword, String encodedPassword) {
        return Mono.fromCallable(() -> {
            log.info("DEBUG - Simple password validation: Raw='{}', Stored='{}'", rawPassword, encodedPassword);
            
            // TEMPORAL: Validación simple sin encriptación para pruebas rápidas
            boolean result = rawPassword.equals(encodedPassword);
            log.info("DEBUG - Simple match result: {}", result);
            return result;
            
            /* COMENTADO: Validación BCrypt completa
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
                log.info("DEBUG - BCrypt validation result: {}", result);
                return result;
            } catch (Exception e) {
                log.error("BCrypt validation failed with exception: {}", e.getMessage(), e);
                return false;
            }
            */
        })
        .doOnNext(matches -> log.debug("Final password validation result: {}", matches))
        .onErrorReturn(false);
    }

    @Override
    public Mono<String> encode(String rawPassword) {
        return Mono.fromCallable(() -> {
            // TEMPORAL: Retornar contraseña sin encriptar para pruebas rápidas
            log.info("DEBUG - Returning plain password: '{}'", rawPassword);
            return rawPassword;
            
            /* COMENTADO: Generación BCrypt completa
            String encoded = encoder.encode(rawPassword);
            log.info("DEBUG - Generated BCrypt hash: '{}' for password: '{}'", encoded, rawPassword);
            log.info("DEBUG - Hash length: {}, Format valid: {}", encoded.length(), encoded.startsWith("$2a$"));
            return encoded;
            */
        })
        .doOnNext(encoded -> log.debug("Password encoded successfully"))
        .onErrorMap(Exception.class, ex -> {
            log.error("Error encoding password", ex);
            return new RuntimeException("Failed to encode password", ex);
        });
    }
}
