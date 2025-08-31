package co.com.crediya.authentication.api.mapper;

import co.com.crediya.authentication.api.dto.CreateUserRequest;
import co.com.crediya.authentication.api.dto.UserResponse;
import co.com.crediya.authentication.model.user.User;
import co.com.crediya.authentication.model.user.UserType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", ignore = true)
    User toUser(CreateUserRequest request);

    @Mapping(target = "role", source = "role")
    @Mapping(target = "userType", expression = "java(user.getRole() != null ? user.getRole().getName() : null)")
    UserResponse toUserResponse(User user);
    
    default UserType mapUserType(CreateUserRequest request) {
        return UserType.fromCode(request.getUserType());
    }
}
