package com.hanihome.hanihome_au_api.validation;

import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PropertyTypeValidator implements ConstraintValidator<ValidPropertyType, PropertyType> {

    @Override
    public void initialize(ValidPropertyType constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(PropertyType value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null values are handled by @NotNull
        }
        
        // Additional business logic validation can be added here
        // For example, checking if certain property types are allowed based on context
        return true;
    }
}