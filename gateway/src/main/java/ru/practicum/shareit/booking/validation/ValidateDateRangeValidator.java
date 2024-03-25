package ru.practicum.shareit.booking.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.time.LocalDateTime;

public class ValidateDateRangeValidator implements ConstraintValidator<ValidateDateRange, Object> {

    String start;
    String end;
    String message;

    @Override
    public void initialize(ValidateDateRange constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        start = constraintAnnotation.start();
        end = constraintAnnotation.end();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        try {
            Field startField = object.getClass().getDeclaredField(start);
            startField.setAccessible(true);
            Field endField = object.getClass().getDeclaredField(end);
            endField.setAccessible(true);

            LocalDateTime startDate = (LocalDateTime) startField.get(object);
            LocalDateTime endDate = (LocalDateTime) endField.get(object);
            if (startDate == null || endDate == null) {
                return false;
            }
            return endDate.isAfter(startDate) && !endDate.isEqual(startDate) &&
                    !endDate.isEqual(LocalDateTime.now()) && startDate.isAfter(LocalDateTime.now());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
    }
}
