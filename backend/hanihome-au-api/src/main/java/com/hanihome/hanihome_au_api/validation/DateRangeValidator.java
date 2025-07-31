package com.hanihome.hanihome_au_api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDate;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    private String fromField;
    private String toField;

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.fromField = constraintAnnotation.fromField();
        this.toField = constraintAnnotation.toField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        try {
            Field fromFieldRef = ReflectionUtils.findField(value.getClass(), fromField);
            Field toFieldRef = ReflectionUtils.findField(value.getClass(), toField);

            if (fromFieldRef == null || toFieldRef == null) {
                return true; // Can't validate if fields don't exist
            }

            fromFieldRef.setAccessible(true);
            toFieldRef.setAccessible(true);

            LocalDate fromDate = (LocalDate) fromFieldRef.get(value);
            LocalDate toDate = (LocalDate) toFieldRef.get(value);

            if (fromDate == null || toDate == null) {
                return true; // null values are valid
            }

            return !fromDate.isAfter(toDate);
        } catch (Exception e) {
            return false;
        }
    }
}