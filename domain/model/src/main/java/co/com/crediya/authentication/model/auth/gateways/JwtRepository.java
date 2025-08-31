package co.com.crediya.authentication.model.auth.gateways;

import co.com.crediya.authentication.model.auth.JwtToken;
import co.com.crediya.authentication.model.user.User;
import reactor.core.publisher.Mono;

public interface JwtRepository {
    Mono<JwtToken> generateToken(User user);

    Mono<Boolean> validateToken(String token);

    String extractEmail(String token);

    String extractRole(String token);

    Long extractUserId(String token);
}