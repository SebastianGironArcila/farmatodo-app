package co.com.farmatodo.api.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ValidationHandler {
    private final Validator validator;

    public <T> Mono<T> validate(T obj) {
        Set<ConstraintViolation<T>> violations = validator.validate(obj);
        if (!violations.isEmpty()) {
            return Mono.error(new ConstraintViolationException(violations));
        }
        return Mono.just(obj);
    }
}
