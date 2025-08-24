package co.com.crediya.authentication.r2dbc.mapper;

import co.com.crediya.authentication.model.user.User;
import co.com.crediya.authentication.r2dbc.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    @Mapping(target = "roleId", source = "role.id")
    UserEntity toEntity(User user);

    @Mapping(target = "role", ignore = true)
    User toDomain(UserEntity entity);
}
