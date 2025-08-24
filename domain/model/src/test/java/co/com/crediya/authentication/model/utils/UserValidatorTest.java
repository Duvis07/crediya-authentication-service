package co.com.crediya.authentication.model.utils;

import co.com.crediya.authentication.model.exceptions.InvalidUserDataException;
import co.com.crediya.authentication.model.user.User;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

class UserValidatorTest {

    private User validUser() {
        User user = new User();
        user.setFirstName("Juan");
        user.setLastName("Pérez");
        user.setEmail("juan.perez@example.com");
        user.setBaseSalary(BigDecimal.valueOf(5_000_000));
        user.setBirthDate(LocalDate.of(1990, 5, 15));
        return user;
    }

    @Test
    void shouldValidateCorrectUser() {
        User user = validUser();

        StepVerifier.create(UserValidator.validateUserData(user))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenFirstNameMissing() {
        User user = validUser();
        user.setFirstName(null);

        StepVerifier.create(UserValidator.validateUserData(user))
                .expectErrorSatisfies(throwable ->
                        org.assertj.core.api.Assertions.assertThat(throwable)
                                .isInstanceOf(InvalidUserDataException.class)
                                .hasMessageContaining("firstName"))
                .verify();
    }

    @Test
    void shouldFailWhenInvalidEmail() {
        User user = validUser();
        user.setEmail("invalid-email");

        StepVerifier.create(UserValidator.validateUserData(user))
                .expectErrorSatisfies(throwable ->
                        org.assertj.core.api.Assertions.assertThat(throwable)
                                .isInstanceOf(InvalidUserDataException.class)
                                .hasMessageContaining("Invalid email format"))
                .verify();
    }

    @Test
    void shouldFailWhenSalaryTooHigh() {
        User user = validUser();
        user.setBaseSalary(BigDecimal.valueOf(20_000_000));

        StepVerifier.create(UserValidator.validateUserData(user))
                .expectError(InvalidUserDataException.class)
                .verify();
    }

    @Test
    void shouldFailWhenBirthDateInFuture() {
        User user = validUser();
        user.setBirthDate(LocalDate.now().plusDays(1));

        StepVerifier.create(UserValidator.validateUserData(user))
                .expectErrorSatisfies(throwable ->
                        org.assertj.core.api.Assertions.assertThat(throwable)
                                .isInstanceOf(InvalidUserDataException.class)
                                .hasMessageContaining("Birth date cannot be in the future"))
                .verify();
    }
}
