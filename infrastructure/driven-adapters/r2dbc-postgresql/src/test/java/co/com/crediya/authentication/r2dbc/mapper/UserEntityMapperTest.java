package co.com.crediya.authentication.r2dbc.mapper;

import co.com.crediya.authentication.r2dbc.entity.UserEntity;
import co.com.crediya.authentication.model.role.Role;
import co.com.crediya.authentication.model.user.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityMapperTest {

    private final UserEntityMapper mapper = Mappers.getMapper(UserEntityMapper.class);

    @Test
    void toEntityShouldMapUserToUserEntityCorrectly() {
        Role role = Role.builder()
                .id(10L)
                .name("Solicitante")
                .build();

        User user = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .address("Calle 123")
                .phone("3001234567")
                .baseSalary(BigDecimal.valueOf(2000.0))
                .role(role)
                .build();

        UserEntity entity = mapper.toEntity(user);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getFirstName()).isEqualTo("Juan");
        assertThat(entity.getLastName()).isEqualTo("Pérez");
        assertThat(entity.getEmail()).isEqualTo("juan.perez@example.com");
        assertThat(entity.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(entity.getAddress()).isEqualTo("Calle 123");
        assertThat(entity.getPhone()).isEqualTo("3001234567");
        assertThat(entity.getBaseSalary()).isEqualTo(BigDecimal.valueOf(2000.0));
        assertThat(entity.getRoleId()).isEqualTo(10L); // mapeo de role.id a roleId
    }

    @Test
    void toDomainShouldMapUserEntityToUserCorrectly() {
        UserEntity entity = UserEntity.builder()
                .id(1L)
                .firstName("Ana")
                .lastName("Gómez")
                .email("ana.gomez@example.com")
                .birthDate(LocalDate.of(1995, 5, 15))
                .address("Carrera 45")
                .phone("3119876543")
                .baseSalary(BigDecimal.valueOf(3000.0))
                .roleId(20L)
                .build();

        User user = mapper.toDomain(entity);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getFirstName()).isEqualTo("Ana");
        assertThat(user.getLastName()).isEqualTo("Gómez");
        assertThat(user.getEmail()).isEqualTo("ana.gomez@example.com");
        assertThat(user.getBirthDate()).isEqualTo(LocalDate.of(1995, 5, 15));
        assertThat(user.getAddress()).isEqualTo("Carrera 45");
        assertThat(user.getPhone()).isEqualTo("3119876543");
        assertThat(user.getBaseSalary()).isEqualTo(BigDecimal.valueOf(3000.0));
        assertThat(user.getRole()).isNull(); // porque el mapper ignora el campo role
    }

    @Test
    void toEntityShouldReturnNullWhenUserIsNull() {
        UserEntity entity = mapper.toEntity(null);
        assertThat(entity).isNull();
    }

    @Test
    void toDomainShouldReturnNullWhenEntityIsNull() {
        User user = mapper.toDomain(null);
        assertThat(user).isNull();
    }
}
