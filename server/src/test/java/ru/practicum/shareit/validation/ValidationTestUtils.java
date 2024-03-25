package ru.practicum.shareit.validation;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.constraints.NotNull;
import java.util.Set;

public class ValidationTestUtils {
    public static final javax.validation.Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private ValidationTestUtils() {

    }

    public static <T> boolean dtoHasErrorMessage(T dto, @NotNull String message) {
        Set<ConstraintViolation<T>> errors = VALIDATOR.validate(dto);
        return errors.stream().map(ConstraintViolation::getMessage).anyMatch(message::equals);
    }
}
