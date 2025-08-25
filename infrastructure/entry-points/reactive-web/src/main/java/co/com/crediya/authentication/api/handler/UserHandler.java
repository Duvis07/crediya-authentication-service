package co.com.crediya.authentication.api.handler;

import co.com.crediya.authentication.api.dto.CreateUserRequest;
import co.com.crediya.authentication.api.mapper.UserMapper;
import co.com.crediya.authentication.usecase.UserUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class UserHandler extends BaseHandler {

    private final UserUseCase userUseCase;
    private final UserMapper userMapper;

    public UserHandler(UserUseCase userUseCase, UserMapper userMapper, Validator validator) {
        super(validator);
        this.userUseCase = userUseCase;
        this.userMapper = userMapper;
    }


    private static final String MESSAGE_KEY = "message";
    private static final String USER_CREATED_MESSAGE = "User successfully created";
    private static final String USER_UPDATED_MESSAGE = "User successfully updated";
    private static final String USER_DELETED_MESSAGE = "User successfully deleted";

    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(CreateUserRequest.class)
                .flatMap(this::validate)
                .flatMap(createUserRequest -> {
                    var user = userMapper.toUser(createUserRequest);
                    var userType = userMapper.mapUserType(createUserRequest);
                    return userUseCase.createUser(user, userType);
                })
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

}