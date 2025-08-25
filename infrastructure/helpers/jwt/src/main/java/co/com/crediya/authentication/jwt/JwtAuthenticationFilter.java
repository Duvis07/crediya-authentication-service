package co.com.crediya.authentication.jwt;

import co.com.crediya.authentication.model.auth.gateways.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // Skip authentication for login endpoint
        if (path.equals("/api/v1/login")) {
            return chain.filter(exchange);
        }
        
        // Skip authentication for non-protected endpoints
        if (!isProtectedEndpoint(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);
        
        return jwtService.validateToken(token)
            .flatMap(isValid -> {
                if (!isValid) {
                    log.warn("Invalid JWT token for path: {}", path);
                    return unauthorized(exchange);
                }

                // Extract user info and add to exchange attributes
                String email = jwtService.extractEmail(token);
                String role = jwtService.extractRole(token);
                Long userId = jwtService.extractUserId(token);
                
                exchange.getAttributes().put("user.email", email);
                exchange.getAttributes().put("user.role", role);
                exchange.getAttributes().put("user.id", userId);
                
                log.info("Authenticated user: {} with role: {} for path: {}", email, role, path);
                
                return chain.filter(exchange);
            });
    }

    private boolean isProtectedEndpoint(String path) {
        return path.startsWith("/api/v1/usuarios");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        String body = "{\"error\":\"Unauthorized\",\"message\":\"Valid JWT token required\"}";
        return exchange.getResponse().writeWith(
            Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
        );
    }
}
