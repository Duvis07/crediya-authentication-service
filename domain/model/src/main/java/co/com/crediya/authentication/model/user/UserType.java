package co.com.crediya.authentication.model.user;

import co.com.crediya.authentication.model.exceptions.InvalidUserDataException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;


import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum UserType {
    APPLICANT("Solicitante", "APPLICANT"),
    ADMIN("Administrador", "ADMIN"),
    ASESOR("Asesor", "ASESOR");

    private final String roleName;
    private final String code;

    public static UserType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidUserDataException("User type is required. Valid values: APPLICANT, ADMIN, ASESOR");
        }

        return Arrays.stream(values())
                .filter(type -> type.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new InvalidUserDataException("Invalid user type: " + code + ". Valid values: APPLICANT, ADMIN, ASESOR"));
    }
}
