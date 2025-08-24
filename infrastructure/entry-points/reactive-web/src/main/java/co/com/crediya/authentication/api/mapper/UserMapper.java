package co.com.crediya.authentication.api.mapper;

import co.com.crediya.authentication.api.dto.CreateUserRequest;
import co.com.crediya.authentication.api.dto.UserResponse;
import co.com.crediya.authentication.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(CreateUserRequest request);

    @Mapping(target = "role", source = "role")
    UserResponse toUserResponse(User user);
}
