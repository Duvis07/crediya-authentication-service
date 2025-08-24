package co.com.crediya.authentication.r2dbc.mapper;

import co.com.crediya.authentication.model.role.Role;
import co.com.crediya.authentication.r2dbc.entity.RoleEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleEntityMapper {

    Role toModel(RoleEntity entity);

}
