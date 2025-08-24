package co.com.crediya.authentication.api;

import co.com.crediya.authentication.api.dto.CreateUserRequest;
import co.com.crediya.authentication.api.dto.UserResponse;
import co.com.crediya.authentication.api.handler.UserHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
                    path = "/api/v1/usuarios/{id}",
                    method = RequestMethod.GET,
                    operation = @Operation(
                            operationId = "getUserById",
                            summary = "Obtener usuario por ID",
                            description = "Obtiene la información de un usuario específico mediante su ID",
                            tags = {"Usuarios"},
                            parameters = @Parameter(name = "id", description = "ID del usuario", required = true),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Usuario encontrado",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(implementation = UserResponse.class)
                                            )
                                    ),
                                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                                    @ApiResponse(responseCode = "400", description = "ID inválido")
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
            @RouterOperation(
                    path = "/api/v1/usuarios/{id}",
                    method = RequestMethod.PUT,
                    operation = @Operation(
                            operationId = "updateUser",
                            summary = "Actualizar usuario",
                            description = "Actualiza la información de un usuario existente",
                            tags = {"Usuarios"},
                            parameters = @Parameter(name = "id", description = "ID del usuario", required = true),
                            requestBody = @RequestBody(
                                    description = "Datos actualizados del usuario",
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = CreateUserRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Usuario actualizado exitosamente",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(implementation = UserResponse.class)
                                            )
                                    ),
                                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                                    @ApiResponse(responseCode = "400", description = "Datos inválidos o ID inválido"),
                                    @ApiResponse(responseCode = "409", description = "El correo electrónico ya está registrado por otro usuario")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/usuarios/{id}",
                    method = RequestMethod.DELETE,
                    operation = @Operation(
                            operationId = "deleteUser",
                            summary = "Eliminar usuario",
                            description = "Elimina un usuario del sistema",
                            tags = {"Usuarios"},
                            parameters = @Parameter(name = "id", description = "ID del usuario", required = true),
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
                                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                                    @ApiResponse(responseCode = "400", description = "ID inválido")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> userRoutes() {
        return route(POST("/api/v1/usuarios")
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                userHandler::createUser)
                .andRoute(GET("/api/v1/usuarios/{id}")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        userHandler::getUserById)
                .andRoute(GET("/api/v1/usuarios")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        userHandler::getAllUsers)
                .andRoute(PUT("/api/v1/usuarios/{id}")
                        .and(accept(MediaType.APPLICATION_JSON))
                        .and(contentType(MediaType.APPLICATION_JSON)),
                        userHandler::updateUser)
                .andRoute(DELETE("/api/v1/usuarios/{id}")
                        .and(accept(MediaType.APPLICATION_JSON)),
                        userHandler::deleteUser);
    }
}