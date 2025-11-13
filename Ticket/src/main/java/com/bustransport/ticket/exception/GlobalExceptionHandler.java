package com.bustransport.ticket.exception;

import com.bustransport.ticket.dto.response.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
        ResourceNotFoundException ex, WebRequest request) {
        log.error("Resource not found: {}", ex.getMessage());

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InvalidTicketException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidTicketException(
        InvalidTicketException ex, WebRequest request) {
        log.error("Invalid ticket: {}", ex.getMessage());

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Invalid Ticket")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentFailedException(
        PaymentFailedException ex, WebRequest request) {
        log.error("Payment failed: {}", ex.getMessage());

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.PAYMENT_REQUIRED.value())
            .error("Payment Failed")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(errorResponse);
    }

    @ExceptionHandler(RefundException.class)
    public ResponseEntity<ApiErrorResponse> handleRefundException(
        RefundException ex, WebRequest request) {
        log.error("Refund error: {}", ex.getMessage());

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Refund Error")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(
        IllegalStateException ex, WebRequest request) {
        log.error("Illegal state: {}", ex.getMessage());

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.CONFLICT.value())
            .error("Conflict")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation failed: {}", ex.getMessage());

        List<String> details = ex.getBindingResult().getAllErrors().stream()
            .map(error -> {
                if (error instanceof FieldError) {
                    return ((FieldError) error).getField() + ": " + error.getDefaultMessage();
                }
                return error.getDefaultMessage();
            })
            .collect(Collectors.toList());

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Invalid request parameters")
            .details(details)
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGlobalException(
        Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
