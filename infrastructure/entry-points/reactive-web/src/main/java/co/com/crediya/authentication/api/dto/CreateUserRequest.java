package co.com.crediya.authentication.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @JsonProperty("first_name")
    String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @JsonProperty("last_name")
    String lastName;

    @Past(message = "Birth date must be in the past")
    @JsonProperty("birth_date")
    LocalDate birthDate;

    @Size(max = 200, message = "Address must not exceed 200 characters")
    @JsonProperty("address")
    String address;

    @Pattern(regexp = "^[+]?\\d{10,15}$", message = "Phone number format is invalid")
    @JsonProperty("phone")
    String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @JsonProperty("email")
    String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @JsonProperty("password")
    String password;

    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", message = "Base salary must be greater or equal to 0")
    @DecimalMax(value = "15000000.0", message = "Base salary must not exceed 15,000,000")
    @JsonProperty("base_salary")
    BigDecimal baseSalary;

    @NotBlank(message = "User type is required")
    @Pattern(regexp = "APPLICANT|ADMIN", message = "User type must be APPLICANT or ADMIN")
    @JsonProperty("user_type")
    String userType;
}
