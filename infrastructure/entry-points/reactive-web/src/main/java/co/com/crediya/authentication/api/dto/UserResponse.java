package co.com.crediya.authentication.api.dto;

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

    String firstName;

    String lastName;

    LocalDate birthDate;

    String address;

    String phone;

    String email;

    BigDecimal baseSalary;

    LocalDateTime createdAt;

    RoleResponse role;
}
