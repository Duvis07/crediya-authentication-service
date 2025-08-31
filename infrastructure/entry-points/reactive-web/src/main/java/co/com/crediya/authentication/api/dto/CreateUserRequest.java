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

    @NotBlank(message = "Document ID is required")
    @Size(max = 20, message = "Document ID must not exceed 20 characters")
    String documentId;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    String lastName;

    @Past(message = "Birth date must be in the past")
    LocalDate birthDate;

    @Size(max = 200, message = "Address must not exceed 200 characters")
    @JsonProperty("address")
    String address;

    @Pattern(regexp = "^[+]?\\d{10,15}$", message = "Phone number format is invalid")
    String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    String password;

    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", message = "Base salary must be greater or equal to 0")
    @DecimalMax(value = "15000000.0", message = "Base salary must not exceed 15,000,000")
    BigDecimal baseSalary;

    @NotBlank(message = "User type is required")
    @Pattern(regexp = "APPLICANT|ADMIN|ASESOR", message = "User type must be one of the following: APPLICANT, ADMIN, ASESOR")
    String userType;
}
