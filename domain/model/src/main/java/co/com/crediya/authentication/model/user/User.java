package co.com.crediya.authentication.model.user;

import co.com.crediya.authentication.model.role.Role;
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
public class User {
    private Long id;
    private String documentId;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String address;
    private String phone;
    private String email;
    private String password;
    private BigDecimal baseSalary;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
