package co.com.crediya.authentication.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((exchange, ex) -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                    
                    String body = "{\"error\":\"Unauthorized\",\"status\":401,\"message\":\"Authentication required\",\"timestamp\":\"" + 
                                  java.time.Instant.now() + "\"}";
                    
                    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
                    return exchange.getResponse().writeWith(Mono.just(buffer));
                })
                .accessDeniedHandler((exchange, denied) -> {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                    
                    String body = "{\"error\":\"Forbidden\",\"status\":403,\"message\":\"Access denied - insufficient privileges\",\"timestamp\":\"" + 
                                  java.time.Instant.now() + "\"}";
                    
                    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
                    return exchange.getResponse().writeWith(Mono.just(buffer));
                })
            )
            .authorizeExchange(exchanges -> exchanges
                // Endpoints públicos (sin autenticación)
                .pathMatchers(HttpMethod.POST, "/api/v1/login").permitAll()
                
                // Swagger/OpenAPI (público)
                .pathMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .pathMatchers("/webjars/**", "/swagger-resources/**").permitAll()
                
                // Actuator endpoints (público para desarrollo)
                .pathMatchers("/actuator/**").permitAll()
                
                // Todos los demás endpoints requieren autenticación (manejada por JwtAuthenticationFilter)
                .anyExchange().permitAll()
            )
            .build();
    }
}
