package com.pharmasync.branch.exception;

import com.pharmasync.domain.exception.InsufficientStockException;
import com.pharmasync.domain.exception.InvalidBatchStateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler returning RFC 9457 Problem Details for all API errors.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String BASE_TYPE_URI = "https://pharmasync.com/errors/";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed for request parameters"
        );
        problem.setTitle("Validation Error");
        problem.setType(URI.create(BASE_TYPE_URI + "validation-error"));
        problem.setProperty("timestamp", Instant.now().toString());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        problem.setProperty("invalidFields", fieldErrors);
        return problem;
    }

    @ExceptionHandler(PrescriptionRequiredException.class)
    public ProblemDetail handlePrescriptionRequired(PrescriptionRequiredException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage()
        );
        problem.setTitle("Prescription Required");
        problem.setType(URI.create(BASE_TYPE_URI + "prescription-required"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ProblemDetail handleInsufficientStock(InsufficientStockException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage()
        );
        problem.setTitle("Insufficient Stock");
        problem.setType(URI.create(BASE_TYPE_URI + "insufficient-stock"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler(InvalidBatchStateException.class)
    public ProblemDetail handleInvalidBatchState(InvalidBatchStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage()
        );
        problem.setTitle("Invalid Batch State");
        problem.setType(URI.create(BASE_TYPE_URI + "invalid-batch-state"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage()
        );
        problem.setTitle("Resource Not Found");
        problem.setType(URI.create(BASE_TYPE_URI + "not-found"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage()
        );
        problem.setTitle("Bad Request");
        problem.setType(URI.create(BASE_TYPE_URI + "bad-request"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralException(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal server error occurred"
        );
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create(BASE_TYPE_URI + "internal-error"));
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("errorDetails", ex.getMessage());
        return problem;
    }
}
