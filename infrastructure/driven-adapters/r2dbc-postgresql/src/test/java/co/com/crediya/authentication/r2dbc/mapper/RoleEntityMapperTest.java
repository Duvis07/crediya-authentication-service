package co.com.crediya.authentication.r2dbc.mapper;

import co.com.crediya.authentication.r2dbc.entity.RoleEntity;
import co.com.crediya.authentication.model.role.Role;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class RoleEntityMapperTest {

    private final RoleEntityMapper mapper = Mappers.getMapper(RoleEntityMapper.class);

    @Test
    void toModelShouldMapFieldsCorrectly() {
        RoleEntity entity = RoleEntity.builder()
                .id(1L)
                .name("Solicitante")
                .build();

        Role role = mapper.toModel(entity);

        assertThat(role).isNotNull();
        assertThat(role.getId()).isEqualTo(1L);
        assertThat(role.getName()).isEqualTo("Solicitante");
    }

    @Test
    void toModelShouldReturnNullWhenEntityIsNull() {
        Role role = mapper.toModel(null);

        assertThat(role).isNull();
    }
}
