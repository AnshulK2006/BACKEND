package com.cvns.exception_handler;

import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.cvns.custom_exceptions.ApiException;
import com.cvns.custom_exceptions.ResourceNotFoundException;
import com.cvns.dtos.ResponseDtos.ApiResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<?> notFound(ResourceNotFoundException e){return ResponseEntity.status(404).body(ApiResponse.failure(e.getMessage()));}

    @ExceptionHandler({ApiException.class,IllegalArgumentException.class})
    ResponseEntity<?> bad(RuntimeException e){return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));}

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<?> auth(AuthenticationException e){return ResponseEntity.status(401).body(ApiResponse.failure("Invalid email or password"));}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> valid(MethodArgumentNotValidException e){
        String m=e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ApiResponse.failure(m));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> conflict(DataIntegrityViolationException e){return ResponseEntity.status(409).body(ApiResponse.failure("Duplicate or conflicting data"));}

    @ExceptionHandler(Exception.class)
    ResponseEntity<?> other(Exception e){log.error("Unhandled server error",e);return ResponseEntity.status(500).body(ApiResponse.failure("Unexpected server error"));}
}
