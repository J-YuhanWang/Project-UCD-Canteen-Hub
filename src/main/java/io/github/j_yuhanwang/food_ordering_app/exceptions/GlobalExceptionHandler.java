package io.github.j_yuhanwang.food_ordering_app.exceptions;

import io.github.j_yuhanwang.food_ordering_app.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Global Exception Handler.
 * Acts as the centralized crisis management center to transform exceptions into standardized JSON responses.
 * @author YuhanWang
 * @Date 04/02/2026 10:14 pm
 */
@Slf4j
@RestControllerAdvice //Intercept global exceptions
public class GlobalExceptionHandler {
    /**
     * Helper method to build a consistent ResponseEntity.
     * Maps the internal business response to the external HTTP protocol.
     * * @param status The HTTP status to return (e.g., 404 NOT_FOUND)
     * @param message The error message to display to the user
     * @return A wrapped ResponseEntity containing the standard Response object
     */
    private ResponseEntity<Response<?>> buildErrorResponse(HttpStatus status, String message){
        Response<?> response = Response.builder()
                .statusCode(status.value()) //404
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response,status);
    }

    // =========================================================================
    // CLIENT ERRORS (4xx) - Issues caused by user input or authentication
    // =========================================================================

    /**
     * Handle 400 Bad Request: Triggered when client input logic fails validation.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Response<?>> handlerBadRequest(BadRequestException ex){
        log.warn("[400 BAD_REQUEST] Business logic failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST,ex.getMessage());
    }

    /**
     * Handle 401 Unauthorized: Triggered when the user is not logged in or the token is invalid.
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Response<?>> handlerUnauthorizedAccess(UnauthorizedAccessException ex){
        log.warn("[401 UNAUTHORIZED] Authentication failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED,ex.getMessage());
    }

    /**
     * Handle 403 Forbidden: Triggered when a logged-in user lacks the required permissions.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response<?>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("[403 FORBIDDEN] Access denied for current user: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Access Denied: You do not have permission to access this resource."+ex.getMessage());
    }


    /**
     * Handle 404 Not Found: Triggered when a requested resource (User, Menu, Order) does not exist.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Response<?>> handleResourceNotFound(ResourceNotFoundException ex){
        log.warn("[404 NOT_FOUND] Resource lookup failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND,ex.getMessage());
    }

    /**
     * Handle 409 Conflict: Triggered when a resource already exists (e.g., duplicate email during registration).
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Response<?>> handlerUserAlreadyExists(UserAlreadyExistsException ex){
        log.warn("[409 CONFLICT] Resource already exists: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT,ex.getMessage());
    }

    /**
     * Handle MethodArgumentNotValidException: 400
     * Triggered automatically by Spring when @Valid validation fails in Controller.
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Response<?>> handleValidationException(MethodArgumentNotValidException ex) {
        //Extract the error message from the first failed validation (or concatenate all errors).
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(org.springframework.validation.FieldError::getDefaultMessage)
                .collect(java.util.stream.Collectors.joining(", "));

        //IMPORTANT!
        log.warn("[400 VALIDATION_FAILED] Client input validation error: {}", errorMessage);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed: " + errorMessage);
    }

    // =========================================================================
    // SERVER & EXTERNAL ERRORS (5xx) - Issues with third-party services or system failures
    // =========================================================================

    /**
     * Handle 502 Bad Gateway: Specific to issues with the external payment provider (e.g., Stripe).
     */
    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<Response<?>> handlerPaymentProcessing(PaymentProcessingException ex){
        log.error("[502 BAD_GATEWAY] External payment gateway error: ", ex);
        return buildErrorResponse(HttpStatus.BAD_GATEWAY,ex.getMessage());
    }

    /**
     * Handle 503 Service Unavailable: Used when the external Email service is unreachable.
     */
    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<Response<?>> handlerEmailDeliveryError(EmailDeliveryException ex){
        log.error("[503 SERVICE_UNAVAILABLE] Email delivery service failed: ", ex);
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE,"Email service busy: " + ex.getMessage());
    }

    /**
     * Handle 500 Internal Server Error: Specifically for file storage failures (e.g., AWS S3).
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<Response<?>> handlerFileStorageError(FileStorageException ex){
        log.error("[500 INTERNAL_SERVER_ERROR] File storage operation failed: ", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed: " + ex.getMessage());
    }

    /**
     * Global Fallback Handler: Catches all unhandled exceptions to prevent leaking sensitive system details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<?>> handleGlobalException(Exception ex){
        log.error("[500 UNEXPECTED_ERROR] Critical system failure: ", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + ex.getMessage());
    }

}
