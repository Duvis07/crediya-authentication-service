package co.com.crediya.authentication.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class RoleAuthorizationFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(RoleAuthorizationFilter.class);
    private static final List<String> ADMIN_ASESOR_ROLES = Arrays.asList("ADMIN", "Administrador", "ASESOR", "Asesor");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        HttpMethod method = exchange.getRequest().getMethod();
        
        // Check if this endpoint requires role validation
        if (requiresAdminAsesorRole(path, method)) {
            String userRole = (String) exchange.getAttributes().get("user.role");
            String userEmail = (String) exchange.getAttributes().get("user.email");
            
            if (userRole == null || !ADMIN_ASESOR_ROLES.contains(userRole)) {
                log.warn("Access denied for user: {} with role: {} to endpoint: {} {}", 
                        userEmail, userRole, method, path);
                return forbidden(exchange);
            }
            
            log.info("Access granted for user: {} with role: {} to endpoint: {} {}", 
                    userEmail, userRole, method, path);
        }
        
        return chain.filter(exchange);
    }

    private boolean requiresAdminAsesorRole(String path, HttpMethod method) {
        // Only ADMIN/ASESOR can create users
        return path.equals("/api/v1/usuarios") && HttpMethod.POST.equals(method);
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        String body = "{\"error\":\"Forbidden\",\"message\":\"Insufficient permissions. Only ADMIN or ASESOR roles can perform this action\"}";
        return exchange.getResponse().writeWith(
            Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
        );
    }
}
