package com.popstars.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 把业务错误变成前端能直接显示的文字。
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(body(ex.getMessage()));
    }

    private Map<String, String> body(String message) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("message", message);
        return result;
    }
}
