package co.com.crediya.authentication.api.mapper;

import co.com.crediya.authentication.api.dto.CreateUserRequest;
import co.com.crediya.authentication.api.dto.UserResponse;
import co.com.crediya.authentication.model.role.Role;
import co.com.crediya.authentication.model.user.User;
import co.com.crediya.authentication.model.user.UserType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toUserShouldMapCreateUserRequestToUser() {
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .address("Calle 123")
                .phone("3001234567")
                .baseSalary(BigDecimal.valueOf(2500.0))
                .build();

        User user = mapper.toUser(request);

        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo("Juan");
        assertThat(user.getLastName()).isEqualTo("Pérez");
        assertThat(user.getEmail()).isEqualTo("juan.perez@example.com");
        assertThat(user.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(user.getAddress()).isEqualTo("Calle 123");
        assertThat(user.getPhone()).isEqualTo("3001234567");
        assertThat(user.getBaseSalary()).isEqualTo(BigDecimal.valueOf(2500.0));
    }

    @Test
    void toUserResponseShouldMapUserToUserResponse() {
        Role role = Role.builder()
                .id(10L)
                .name("Solicitante")
                .build();

        User user = User.builder()
                .id(1L)
                .firstName("Ana")
                .lastName("Gómez")
                .email("ana.gomez@example.com")
                .birthDate(LocalDate.of(1995, 5, 15))
                .address("Carrera 45")
                .phone("3119876543")
                .baseSalary(BigDecimal.valueOf(3000.0))
                .role(role)
                .build();

        UserResponse response = mapper.toUserResponse(user);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFirstName()).isEqualTo("Ana");
        assertThat(response.getLastName()).isEqualTo("Gómez");
        assertThat(response.getEmail()).isEqualTo("ana.gomez@example.com");
        assertThat(response.getBirthDate()).isEqualTo(LocalDate.of(1995, 5, 15));
        assertThat(response.getAddress()).isEqualTo("Carrera 45");
        assertThat(response.getPhone()).isEqualTo("3119876543");
        assertThat(response.getBaseSalary()).isEqualTo(BigDecimal.valueOf(3000.0));
        assertThat(response.getRole()).isNotNull();
        assertThat(response.getRole().getId()).isEqualTo(10L);
        assertThat(response.getRole().getName()).isEqualTo("Solicitante");
    }

    @Test
    void toUserShouldReturnNullWhenRequestIsNull() {
        User user = mapper.toUser(null);
        assertThat(user).isNull();
    }

    @Test
    void toUserResponseShouldReturnNullWhenUserIsNull() {
        UserResponse response = mapper.toUserResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void mapUserTypeShouldReturnApplicantWhenUserTypeIsApplicant() {
        CreateUserRequest request = CreateUserRequest.builder()
                .userType("APPLICANT")
                .build();

        UserType userType = mapper.mapUserType(request);

        assertThat(userType).isEqualTo(UserType.APPLICANT);
    }

    @Test
    void mapUserTypeShouldReturnAdminWhenUserTypeIsAdmin() {
        CreateUserRequest request = CreateUserRequest.builder()
                .userType("ADMIN")
                .build();

        UserType userType = mapper.mapUserType(request);

        assertThat(userType).isEqualTo(UserType.ADMIN);
    }
}
