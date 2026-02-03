package com.tcc.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex, HttpServletRequest request){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Resource not found");
        problemDetail.setProperty("errorCode","RESOURCE_NOT_FOUND");
        problemDetail.setProperty("path", request.getRequestURI());
        return problemDetail;
    }

    @ExceptionHandler(ResourceBadRequest.class)
    public ProblemDetail handleBadRequest(ResourceBadRequest ex, HttpServletRequest request){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problem.setTitle("Resource bad request");
        problem.setProperty("errorCode","RESOURCE_BAD_REQUEST");
        problem.setProperty("path",request.getRequestURI());
        return problem;
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex, HttpServletRequest request){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problem.setTitle("Conflict");
        problem.setProperty("errorCode","CONFLICT");
        problem.setProperty("path",request.getRequestURI());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex , HttpServletRequest request){
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problem.setTitle("Validation Error");
        problem.setProperty("errorCode","BAD_REQUEST");
        problem.setProperty("path",request.getRequestURI());
        return problem;

    }


    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception ex, HttpServletRequest request){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error"
        );
        problem.setTitle("Internal error");
        problem.setProperty("errorCode","INTERNAL_ERROR");
        problem.setProperty("path",request.getRequestURI());
        return problem;
    }
}
