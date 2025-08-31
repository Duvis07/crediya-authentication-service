package co.com.crediya.authentication.api.mapper;

import co.com.crediya.authentication.api.dto.LoginResponse;
import co.com.crediya.authentication.model.auth.JwtToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(source = "token", target = "accessToken")
    LoginResponse toLoginResponse(JwtToken jwtToken);
}
