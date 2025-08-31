package co.com.crediya.authentication.config;

import co.com.crediya.authentication.model.auth.gateways.JwtRepository;
import co.com.crediya.authentication.model.auth.gateways.PasswordEncoderRepository;
import co.com.crediya.authentication.model.role.gateways.RoleRepository;
import co.com.crediya.authentication.model.user.gateways.UserRepository;
import co.com.crediya.authentication.usecase.AuthUseCase;
import co.com.crediya.authentication.usecase.UserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {
    private final UserRepository userRepository;
    private final JwtRepository jwtRepository;
    private final PasswordEncoderRepository passwordEncoderRepository;
    private final RoleRepository roleRepository;

    public UseCasesConfig(UserRepository userRepository, JwtRepository jwtRepository, PasswordEncoderRepository passwordEncoderRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.jwtRepository = jwtRepository;
        this.passwordEncoderRepository = passwordEncoderRepository;
        this.roleRepository = roleRepository;
    }

    @Bean
    public UserUseCase createUserUseCase() {
        return new UserUseCase(userRepository, roleRepository, passwordEncoderRepository);
    }

    @Bean
    public AuthUseCase authUseCase() {
        return new AuthUseCase(userRepository, jwtRepository, passwordEncoderRepository);
    }
}
