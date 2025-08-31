package co.com.crediya.authentication.api.handler;

import co.com.crediya.authentication.api.exceptions.ValidationException;
import org.springframework.http.MediaType;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

abstract class BaseHandler {

    protected final Validator validator;

    protected BaseHandler(Validator validator) {
        this.validator = validator;
    }


    protected <T> Mono<T> validate(T request) {
        Errors errors = new BeanPropertyBindingResult(request, "request");
        validator.validate(request, errors);
        return errors.hasErrors()
                ? Mono.error(new ValidationException("Validation failed", errors))
                : Mono.just(request);
    }

    protected Mono<ServerResponse> okResponse(Object data) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(data);
    }
}
