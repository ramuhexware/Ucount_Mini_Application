package com.freddieapp.notification.util;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

public class ProcessValidationUtil {

    public static Map<String, Object> parseErrors(BindingResult bindingResult, Map<String, Object> outMap) {
        Map<String, Object> errorMap = new HashMap<>(outMap);
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : bindingResult.getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        errorMap.put("status", "VALIDATION_FAILED");
        errorMap.put("errors", fieldErrors);
        return errorMap;
    }
}
