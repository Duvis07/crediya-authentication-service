package co.com.crediya.authentication.api.config;

import co.com.crediya.authentication.api.dto.ErrorResponse;
import co.com.crediya.authentication.jwt.security.JwtReactiveAuthenticationManager;
import co.com.crediya.authentication.jwt.security.JwtServerSecurityContextRepository;
import co.com.crediya.authentication.model.user.UserType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final JwtReactiveAuthenticationManager authenticationManager;
    private final JwtServerSecurityContextRepository securityContextRepository;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtReactiveAuthenticationManager authenticationManager, 
                         JwtServerSecurityContextRepository securityContextRepository,
                         ObjectMapper objectMapper) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.objectMapper = objectMapper;
    }

    private Mono<Void> createErrorResponse(ServerWebExchange exchange, HttpStatus status, String error, String message) {
        try {
            ErrorResponse errorResponse = ErrorResponse.of(error, status.value(), message);
            String body = objectMapper.writeValueAsString(errorResponse);
            
            exchange.getResponse().setStatusCode(status);
            exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            // Fallback en caso de error de serialización
            String fallbackBody = "{\"error\":\"" + error + "\",\"status\":" + status.value() + ",\"message\":\"" + message + "\"}";
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(fallbackBody.getBytes());
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authenticationManager(authenticationManager)
            .securityContextRepository(securityContextRepository)
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((exchange, ex) -> 
                    createErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication required")
                )
                .accessDeniedHandler((exchange, denied) -> 
                    createErrorResponse(exchange, HttpStatus.FORBIDDEN, "Forbidden", "Access denied - insufficient privileges")
                )
            )
            .authorizeExchange(exchanges -> exchanges
                // Endpoints públicos (sin autenticación)
                .pathMatchers(HttpMethod.POST, "/api/v1/login").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v1/usuarios").permitAll()

                // Swagger/OpenAPI (público)
                .pathMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .pathMatchers("/webjars/**", "/swagger-resources/**").permitAll()

                // Actuator endpoints (público para desarrollo)
                .pathMatchers("/actuator/**").permitAll()

                // Endpoints protegidos - requieren autenticación via JWT
                .pathMatchers(HttpMethod.POST, "/api/v1/usuarios").hasAnyRole(UserType.ADMIN.getCode(), UserType.ASESOR.getCode())
                .pathMatchers(HttpMethod.GET, "/api/v1/usuarios/**").permitAll()
                .pathMatchers("/api/v1/solicitud/**").hasRole(UserType.APPLICANT.getCode())

                // Todos los demás endpoints requieren autenticación
                .anyExchange().authenticated()
            )
            .build();
    }
}
