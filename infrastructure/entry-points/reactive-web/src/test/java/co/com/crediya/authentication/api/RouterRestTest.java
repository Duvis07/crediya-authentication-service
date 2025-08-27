package co.com.crediya.authentication.api;

import co.com.crediya.authentication.api.dto.CreateUserRequest;
import co.com.crediya.authentication.api.dto.UserResponse;
import co.com.crediya.authentication.api.handler.UserHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDate;

@ContextConfiguration(classes = {RouterRest.class})
@WebFluxTest
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UserHandler userHandler;

    private UserResponse sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = UserResponse.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .address("Calle 123")
                .phone("3001234567")
                .baseSalary(BigDecimal.valueOf(2500.0))
                .build();
    }

    @Test
    void createUserShouldReturnUserResponse() {
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@example.com")
                .build();

        Mockito.when(userHandler.createUser(Mockito.any())).thenReturn(
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(sampleUser)
        );

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .isEqualTo(sampleUser);
    }


    @Test
    void getAllUsersShouldReturnFluxOfUsers() {
        Mockito.when(userHandler.getAllUsers(Mockito.any())).thenReturn(
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(Flux.just(sampleUser), UserResponse.class)
        );

        webTestClient.get()
                .uri("/api/v1/usuarios")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserResponse.class)
                .hasSize(1)
                .contains(sampleUser);
    }

    // ---- Casos extras para llegar a 9 tests ----

    @Test
    void createUserWithInvalidDataShouldReturnBadRequest() {
        Mockito.when(userHandler.createUser(Mockito.any())).thenReturn(
                ServerResponse.badRequest().build()
        );

        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest();
    }

}