package co.com.crediya.authentication.api.exceptions;

import co.com.crediya.authentication.api.dto.ErrorResponse;
import co.com.crediya.authentication.model.exceptions.InvalidCredentialsException;
import co.com.crediya.authentication.model.exceptions.InvalidUserDataException;
import co.com.crediya.authentication.model.exceptions.UserAlreadyExistsException;
import co.com.crediya.authentication.model.exceptions.UserNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@Order(-2)
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final String VALIDATION_ERROR_MESSAGE = "Validation errors in request data";
    private static final String INVALID_FORMAT_MESSAGE = "Invalid ID format provided";
    private static final String UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred";
    private static final String FIELD_ERROR_FORMAT = "Field '%s' with value '%s': %s";

    private final ObjectMapper objectMapper;

    private final Map<Class<? extends Throwable>, Function<Throwable, ErrorMappingResult>> exceptionMappings = Map.of(
            InvalidCredentialsException.class, ex -> new ErrorMappingResult(HttpStatus.UNAUTHORIZED, ex.getMessage(), List.of()),
            UserAlreadyExistsException.class, ex -> new ErrorMappingResult(HttpStatus.CONFLICT, ex.getMessage(), List.of()),
            UserNotFoundException.class, ex -> new ErrorMappingResult(HttpStatus.NOT_FOUND, ex.getMessage(), List.of()),
            InvalidUserDataException.class, ex -> new ErrorMappingResult(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of()),
            ValidationException.class, this::handleValidationException,
            NumberFormatException.class, ex -> new ErrorMappingResult(HttpStatus.BAD_REQUEST, INVALID_FORMAT_MESSAGE, List.of())
    );

    @Override
    @NonNull
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        log.error("Global exception handler caught: {}", ex.getMessage(), ex);

        ErrorMappingResult errorResult = mapException(ex);
        ErrorResponse errorResponse = createErrorResponse(errorResult);

        return writeErrorResponse(exchange, errorResult.status(), errorResponse);
    }

    private ErrorMappingResult mapException(Throwable ex) {
        return exceptionMappings.entrySet().stream()
                .filter(entry -> entry.getKey().isInstance(ex))
                .findFirst()
                .map(entry -> entry.getValue().apply(ex))
                .orElse(new ErrorMappingResult(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR_MESSAGE, List.of()));
    }

    private ErrorMappingResult handleValidationException(Throwable ex) {
        ValidationException validationEx = (ValidationException) ex;
        List<String> details = extractValidationDetails(validationEx);
        return new ErrorMappingResult(HttpStatus.BAD_REQUEST, VALIDATION_ERROR_MESSAGE, details);
    }

    private List<String> extractValidationDetails(ValidationException validationException) {
        if (validationException.getErrors() == null || !validationException.getErrors().hasFieldErrors()) {
            return List.of();
        }

        List<String> errorDetails = validationException.getErrors().getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList();

        log.warn("Validation error details: {}", errorDetails);
        return errorDetails;
    }

    private String formatFieldError(FieldError fieldError) {
        return String.format(FIELD_ERROR_FORMAT,
                fieldError.getField(),
                fieldError.getRejectedValue(),
                fieldError.getDefaultMessage());
    }

    private ErrorResponse createErrorResponse(ErrorMappingResult errorResult) {
        if (errorResult.details() != null && !errorResult.details().isEmpty()) {
            return ErrorResponse.withDetails(
                    errorResult.status().getReasonPhrase(),
                    errorResult.status().value(),
                    errorResult.message(),
                    errorResult.details()
            );
        } else {
            return ErrorResponse.of(
                    errorResult.status().getReasonPhrase(),
                    errorResult.status().value(),
                    errorResult.message()
            );
        }
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, ErrorResponse errorResponse) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        try {
            String body = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory()
                    .wrap(body.getBytes(StandardCharsets.UTF_8));
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response", e);
            return exchange.getResponse().setComplete();
        }
    }

    private record ErrorMappingResult(HttpStatus status, String message, List<String> details) {
    }
}