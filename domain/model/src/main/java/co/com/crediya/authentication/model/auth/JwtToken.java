package co.com.crediya.authentication.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class JwtToken {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private Long userId;
    private String userRole;
}