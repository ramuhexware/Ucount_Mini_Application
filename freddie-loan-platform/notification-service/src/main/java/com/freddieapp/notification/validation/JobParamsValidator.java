package com.freddieapp.notification.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Map;

@Component("jobParamsValidator")
public class JobParamsValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Map.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (target instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) target;
            // validation logic if needed
        }
    }
}
