package co.com.crediya.authentication.config;

import co.com.crediya.authentication.model.auth.gateways.JwtService;
import co.com.crediya.authentication.model.auth.gateways.PasswordEncoder;
import co.com.crediya.authentication.model.role.gateways.RoleRepository;
import co.com.crediya.authentication.model.user.gateways.UserRepository;
import co.com.crediya.authentication.usecase.AuthUseCase;
import co.com.crediya.authentication.usecase.UserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UseCasesConfig(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Bean
    public UserUseCase createUserUseCase() {
        return new UserUseCase(userRepository, roleRepository);
    }

    @Bean
    public AuthUseCase authUseCase() {
        return new AuthUseCase(userRepository, jwtService, passwordEncoder);
    }
}
