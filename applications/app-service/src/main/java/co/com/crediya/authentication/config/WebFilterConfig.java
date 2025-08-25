package co.com.crediya.authentication.config;

import co.com.crediya.authentication.jwt.JwtAuthenticationFilter;
import co.com.crediya.authentication.jwt.RoleAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

@Configuration
@RequiredArgsConstructor
public class WebFilterConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RoleAuthorizationFilter roleAuthorizationFilter;

    @Bean
    public WebFilter authenticationFilter() {
        return jwtAuthenticationFilter;
    }

    @Bean
    public WebFilter authorizationFilter() {
        return roleAuthorizationFilter;
    }
}
