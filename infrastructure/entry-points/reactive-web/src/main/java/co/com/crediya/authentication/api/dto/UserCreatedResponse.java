package co.com.crediya.authentication.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedResponse {
    private String message;
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    public static UserCreatedResponse success() {
        return UserCreatedResponse.builder()
                .message("Usuario creado exitosamente")
                .status("CREATED")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
