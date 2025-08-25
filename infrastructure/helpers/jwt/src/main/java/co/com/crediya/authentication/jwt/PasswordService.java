package co.com.crediya.authentication.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PasswordService {

    private static final Logger log = LoggerFactory.getLogger(PasswordService.class);
    private final PasswordEncoder passwordEncoder;

    public PasswordService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Mono<String> encode(String rawPassword) {
        return Mono.fromCallable(() -> passwordEncoder.encode(rawPassword))
            .doOnNext(encoded -> log.debug("Password encoded successfully"))
            .onErrorMap(Exception.class, ex -> {
                log.error("Error encoding password", ex);
                return new RuntimeException("Failed to encode password", ex);
            });
    }

    public Mono<Boolean> matches(String rawPassword, String encodedPassword) {
        return Mono.fromCallable(() -> passwordEncoder.matches(rawPassword, encodedPassword))
            .doOnNext(matches -> log.debug("Password validation result: {}", matches))
            .onErrorReturn(false);
    }
}
