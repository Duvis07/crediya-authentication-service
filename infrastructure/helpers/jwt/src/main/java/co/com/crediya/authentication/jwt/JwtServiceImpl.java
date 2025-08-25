package co.com.crediya.authentication.jwt;

import co.com.crediya.authentication.model.auth.JwtToken;
import co.com.crediya.authentication.model.auth.gateways.JwtService;
import co.com.crediya.authentication.model.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtServiceImpl implements JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtServiceImpl.class);
    private final SecretKey secretKey;
    private final long jwtExpiration;

    public JwtServiceImpl(@Value("${jwt.secret:mySecretKey}") String secret,
                         @Value("${jwt.expiration:86400000}") long jwtExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpiration = jwtExpiration;
    }

    @Override
    public Mono<JwtToken> generateToken(User user) {
        return Mono.fromCallable(() -> {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("email", user.getEmail());
            claims.put("role", user.getRole().getName());
            
            String token = createToken(claims, user.getEmail());
            
            return JwtToken.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration / 1000) // Convert to seconds
                    .userId(user.getId())
                    .userRole(user.getRole().getName())
                    .build();
        })
        .doOnNext(jwtToken -> log.debug("JWT token generated for user: {}", user.getEmail()))
        .onErrorMap(Exception.class, ex -> {
            log.error("Error generating JWT token for user: {}", user.getEmail(), ex);
            return new RuntimeException("Failed to generate JWT token", ex);
        });
    }

    @Override
    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
                return true;
            } catch (Exception e) {
                log.debug("Token validation failed: {}", e.getMessage());
                return false;
            }
        })
        .onErrorReturn(false);
    }

    @Override
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    @Override
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}