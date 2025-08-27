package co.com.crediya.authentication.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private String error;
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private List<String> details;

    public static ErrorResponse of(String error, int status, String message) {
        return new ErrorResponse(error, status, message, LocalDateTime.now(), null);
    }

    public static ErrorResponse withDetails(String error, int status, String message, List<String> details) {
        return new ErrorResponse(error, status, message, LocalDateTime.now(), details);
    }
}
