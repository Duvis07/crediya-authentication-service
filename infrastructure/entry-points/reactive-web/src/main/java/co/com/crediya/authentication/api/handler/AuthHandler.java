package co.com.crediya.authentication.api.handler;

import co.com.crediya.authentication.api.dto.LoginRequest;
import co.com.crediya.authentication.api.mapper.AuthMapper;
import co.com.crediya.authentication.usecase.AuthUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthHandler extends BaseHandler {

    private final AuthUseCase authUseCase;
    private final AuthMapper authMapper;

    public AuthHandler(AuthUseCase authUseCase, AuthMapper authMapper, Validator validator) {
        super(validator);
        this.authUseCase = authUseCase;
        this.authMapper = authMapper;
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequest.class)
                .flatMap(this::validate)
                .flatMap(loginRequest -> authUseCase.login(loginRequest.getEmail(), loginRequest.getPassword()))
                .map(authMapper::toLoginResponse)
                .flatMap(this::okResponse);
    }
}
