package co.com.crediya.authentication.config;

import co.com.crediya.authentication.model.role.gateways.RoleRepository;
import co.com.crediya.authentication.model.user.gateways.UserRepository;
import co.com.crediya.authentication.usecase.UserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {
    @Bean
    public UserUseCase createUserUseCase(UserRepository userRepository, RoleRepository roleRepository) {
        return new UserUseCase(userRepository, roleRepository);
    }
}

