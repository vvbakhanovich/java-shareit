package ru.practicum.shareit.shared.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidateDateRangeValidator.class)
public @interface ValidateDateRange {

    String start();

    String end();

    String message() default "Неверно заданы даты.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
