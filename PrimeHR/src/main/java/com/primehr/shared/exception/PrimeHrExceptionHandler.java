package com.primehr.shared.exception;

import com.primehr.shared.api.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import com.primehr.integration.administrative.AuthorizationDependencyException;
import com.primehr.integration.administrative.PositionTargetDependencyException;
import com.primehr.integration.humanresource.HumanResourceDependencyException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class PrimeHrExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(PrimeHrExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(ResourceNotFoundException exception,
                                                      HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request, List.of());
    }

    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiErrorResponse> badRequest(Exception exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Request validation failed", request,
                List.of(exception.getMessage() == null ? "Invalid request" : exception.getMessage()));
    }

    @ExceptionHandler({OptimisticConflictException.class, OptimisticLockingFailureException.class})
    public ResponseEntity<ApiErrorResponse> conflict(Exception exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "OPTIMISTIC_LOCK_CONFLICT", request,
                List.of(exception.getMessage() == null ? "The record was changed by another request" : exception.getMessage()));
    }

    @ExceptionHandler({PublicationConflictException.class, PessimisticLockingFailureException.class})
    public ResponseEntity<ApiErrorResponse> publicationConflict(Exception exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "PUBLICATION_CONFLICT", request,
                List.of(exception.getMessage() == null ? "The definition could not be published concurrently"
                        : exception.getMessage()));
    }

    @ExceptionHandler(IllegalLifecycleTransitionException.class)
    public ResponseEntity<ApiErrorResponse> illegalTransition(IllegalLifecycleTransitionException exception,
                                                               HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "ILLEGAL_LIFECYCLE_TRANSITION", request,
                List.of(exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> forbidden(AccessDeniedException exception, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "Access denied", request, List.of());
    }

    @ExceptionHandler(AuthorizationDependencyException.class)
    public ResponseEntity<ApiErrorResponse> authorizationUnavailable(AuthorizationDependencyException exception,
                                                                      HttpServletRequest request) {
        log.warn("Administrative authorization dependency unavailable for {}", request.getRequestURI());
        return error(HttpStatus.SERVICE_UNAVAILABLE, "AUTHORIZATION_SERVICE_UNAVAILABLE", request, List.of());
    }

    @ExceptionHandler(PositionTargetDependencyException.class)
    public ResponseEntity<ApiErrorResponse> positionTargetUnavailable(PositionTargetDependencyException exception,
                                                                       HttpServletRequest request) {
        log.warn("Administrative position-target dependency unavailable for {}", request.getRequestURI());
        return error(HttpStatus.SERVICE_UNAVAILABLE, "POSITION_TARGET_SERVICE_UNAVAILABLE", request, List.of());
    }

    @ExceptionHandler(HumanResourceDependencyException.class)
    public ResponseEntity<ApiErrorResponse> humanResourceUnavailable(HumanResourceDependencyException exception,
                                                                      HttpServletRequest request) {
        log.warn("HumanResource assessment-subject dependency unavailable for {}", request.getRequestURI());
        return error(HttpStatus.SERVICE_UNAVAILABLE, "HUMAN_RESOURCE_SERVICE_UNAVAILABLE", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> unexpected(Exception exception, HttpServletRequest request) {
        log.error("Unexpected PrimeHR request failure at {}", request.getRequestURI(), exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "The request could not be completed", request, List.of());
    }

    private static ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message,
                                                           HttpServletRequest request, List<String> details) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(), status.value(), message, request.getRequestURI(), details));
    }
}
