package co.com.crediya.authentication.jwt.security;

import co.com.crediya.authentication.model.auth.gateways.JwtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtRepository jwtRepository;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (authentication.getCredentials() == null) {
            return Mono.error(new IllegalArgumentException("Token cannot be null"));
        }
        
        String token = authentication.getCredentials().toString();
        
        return jwtRepository.validateToken(token)
                .filter(isValid -> isValid)
                .map(isValid -> {
                    String email = jwtRepository.extractEmail(token);
                    String role = jwtRepository.extractRole(token);
                    
                    // Create authorities with ROLE_ prefix for Spring Security
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + role)
                    );
                    
                    return new UsernamePasswordAuthenticationToken(
                            email, 
                            null, 
                            authorities
                    );
                })
                .cast(Authentication.class);
    }
}
