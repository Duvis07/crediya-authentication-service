package co.com.crediya.authentication.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
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

    String userType;
}
