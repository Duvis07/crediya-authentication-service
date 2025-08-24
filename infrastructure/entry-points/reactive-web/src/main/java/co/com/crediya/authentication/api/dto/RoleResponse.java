package co.com.crediya.authentication.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class RoleResponse {

    Long id;

    @JsonProperty("name")
    String name;

    @JsonProperty("description")
    String description;
}
