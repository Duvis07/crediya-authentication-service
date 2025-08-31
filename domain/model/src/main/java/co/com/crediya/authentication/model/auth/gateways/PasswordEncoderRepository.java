package co.com.crediya.authentication.model.auth.gateways;

import reactor.core.publisher.Mono;

public interface PasswordEncoderRepository {
    Mono<Boolean> matches(String rawPassword, String encodedPassword);
    Mono<String> encode(String rawPassword);
}
