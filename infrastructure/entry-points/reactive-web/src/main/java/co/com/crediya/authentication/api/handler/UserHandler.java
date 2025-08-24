package co.com.crediya.authentication.api.handler;

import co.com.crediya.authentication.api.dto.CreateUserRequest;
import co.com.crediya.authentication.api.exceptions.ValidationException;
import co.com.crediya.authentication.api.mapper.UserMapper;
import co.com.crediya.authentication.usecase.UserUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserHandler {

    private final UserUseCase userUseCase;
    private final UserMapper userMapper;
    private final Validator validator;


    private static final String MESSAGE_KEY = "message";
    private static final String USER_CREATED_MESSAGE = "User successfully created";
    private static final String USER_UPDATED_MESSAGE = "User successfully updated";
    private static final String USER_DELETED_MESSAGE = "User successfully deleted";

    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(CreateUserRequest.class)
                .flatMap(this::validate)
                .map(userMapper::toUser)
                .flatMap(userUseCase::createUser)
                .flatMap(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put(MESSAGE_KEY, USER_CREATED_MESSAGE);
                    return okResponse(response);
                });
    }

    public Mono<ServerResponse> getUserById(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return userUseCase.findUserById(id)
                .map(userMapper::toUserResponse)
                .flatMap(this::okResponse);
    }

    public Mono<ServerResponse> getAllUsers(ServerRequest request) {
        return userUseCase.findAllUsers()
                .map(userMapper::toUserResponse)
                .collectList()
                .flatMap(this::okResponse);
    }

    public Mono<ServerResponse> updateUser(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return request.bodyToMono(CreateUserRequest.class)
                .flatMap(this::validate)
                .map(userMapper::toUser)
                .flatMap(user -> userUseCase.updateUser(id, user))
                .flatMap(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put(MESSAGE_KEY, USER_UPDATED_MESSAGE);
                    return okResponse(response);
                });
    }

    public Mono<ServerResponse> deleteUser(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return userUseCase.deleteUser(id)
                .then(Mono.fromCallable(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put(MESSAGE_KEY, USER_DELETED_MESSAGE);
                    return response;
                }))
                .flatMap(this::okResponse);
    }

    // UTILITY METHODS

    private Mono<CreateUserRequest> validate(CreateUserRequest request) {
        Errors errors = new BeanPropertyBindingResult(request, "request");
        validator.validate(request, errors);
        return errors.hasErrors()
                ? Mono.error(new ValidationException("Validation failed", errors))
                : Mono.just(request);
    }

    private Mono<ServerResponse> okResponse(Object data) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(data);
    }
}