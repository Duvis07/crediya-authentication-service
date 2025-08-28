package co.com.crediya.authentication.api;

import co.com.crediya.authentication.api.dto.CreateUserRequest;
import co.com.crediya.authentication.api.dto.LoginRequest;
import co.com.crediya.authentication.api.dto.LoginResponse;
import co.com.crediya.authentication.api.dto.UserResponse;
import co.com.crediya.authentication.api.handler.AuthHandler;
import co.com.crediya.authentication.api.handler.UserHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final UserHandler userHandler;
    private final AuthHandler authHandler;

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/usuarios",
                    method = RequestMethod.POST,
                    operation = @Operation(
                            operationId = "createUser",
                            summary = "Registrar nuevo usuario",
                            description = "Registra un nuevo usuario en el sistema proporcionando sus datos personales básicos",
                            tags = {"Usuarios"},
                            requestBody = @RequestBody(
                                    description = "Datos del usuario a crear",
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = CreateUserRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Usuario creado exitosamente",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(implementation = UserResponse.class)
                                            )
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Datos inválidos o errores de validación"),
                                    @ApiResponse(responseCode = "409", description = "El correo electrónico ya está registrado"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/usuarios",
                    method = RequestMethod.GET,
                    operation = @Operation(
                            operationId = "getAllUsers",
                            summary = "Obtener todos los usuarios",
                            description = "Obtiene la lista de todos los usuarios registrados en el sistema",
                            tags = {"Usuarios"},
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Lista de usuarios obtenida exitosamente",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))
                                            )
                                    ),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
    })
    public RouterFunction<ServerResponse> userRoutes() {
        return route(POST("/api/v1/usuarios")
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                userHandler::createUser)
                .andRoute(GET("/api/v1/usuarios")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        userHandler::getAllUsers);
    }

    @Bean
    @RouterOperation(
            path = "/api/v1/login",
            method = RequestMethod.POST,
            operation = @Operation(
                    operationId = "login",
                    summary = "Iniciar sesión",
                    description = "Autentica un usuario y genera un token JWT",
                    tags = {"Autenticación"},
                    requestBody = @RequestBody(
                            description = "Credenciales de acceso",
                            required = true,
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = LoginRequest.class)
                            )
                    ),
                    responses = {
                            @ApiResponse(
                                    responseCode = "200",
                                    description = "Login exitoso",
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = LoginResponse.class)
                                    )
                            ),
                            @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
                            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                    }
            )
    )
    public RouterFunction<ServerResponse> authRoutes() {
        return route(POST("/api/v1/login")
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                authHandler::login);
    }
}