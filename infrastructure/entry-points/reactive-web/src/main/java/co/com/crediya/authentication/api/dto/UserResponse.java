package co.com.crediya.authentication.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class UserResponse {

    Long id;

    @JsonProperty("first_name")
    String firstName;

    @JsonProperty("last_name")
    String lastName;

    @JsonProperty("birth_date")
    LocalDate birthDate;

    @JsonProperty("address")
    String address;

    @JsonProperty("phone")
    String phone;

    @JsonProperty("email")
    String email;

    @JsonProperty("base_salary")
    BigDecimal baseSalary;

    @JsonProperty("created_at")
    LocalDateTime createdAt;

    @JsonProperty("role")
    RoleResponse role;
}
