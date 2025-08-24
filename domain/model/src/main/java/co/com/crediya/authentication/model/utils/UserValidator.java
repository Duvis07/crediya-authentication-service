package co.com.crediya.authentication.model.utils;

import co.com.crediya.authentication.model.exceptions.InvalidUserDataException;
import co.com.crediya.authentication.model.user.User;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;


public class UserValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );
    private static final BigDecimal MIN_SALARY = BigDecimal.ZERO;
    private static final BigDecimal MAX_SALARY = new BigDecimal("15000000");

    private UserValidator() {
        throw new IllegalStateException("Utility class");
    }

    public static Mono<Void> validateUserData(User user) {
        return Mono.fromRunnable(() -> {
            validateRequiredFields(user);
            validateEmailFormat(user.getEmail());
            validateSalaryRange(user.getBaseSalary());
            validateBirthDate(user.getBirthDate());
        });
    }

    private static void validateRequiredFields(User user) {
        if (isNullOrEmpty(user.getFirstName())) {
            throw new InvalidUserDataException("The field 'firstName' is required");
        }

        if (isNullOrEmpty(user.getLastName())) {
            throw new InvalidUserDataException("The field 'lastName' is required");
        }

        if (isNullOrEmpty(user.getEmail())) {
            throw new InvalidUserDataException("The field 'email' is required");
        }

        if (user.getBaseSalary() == null) {
            throw new InvalidUserDataException("The field 'baseSalary' is required");
        }
    }

    private static void validateEmailFormat(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidUserDataException("Invalid email format");
        }
    }

    private static void validateSalaryRange(BigDecimal salary) {
        if (salary.compareTo(MIN_SALARY) < 0 || salary.compareTo(MAX_SALARY) > 0) {
            throw new InvalidUserDataException(
                    "Base salary must be between 0 and 15,000,000");
        }
    }

    private static void validateBirthDate(LocalDate birthDate) {
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new InvalidUserDataException("Birth date cannot be in the future");
        }
        
        if (birthDate != null && birthDate.isBefore(LocalDate.now().minusYears(100))) {
            throw new InvalidUserDataException("Birth date cannot be more than 100 years ago");
        }
    }

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}