package co.com.crediya.authentication.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class RoleResponse {

    Long id;

    String name;

    String description;
}
