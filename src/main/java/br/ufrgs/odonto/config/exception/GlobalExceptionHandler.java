package br.ufrgs.odonto.config.exception;

import br.ufrgs.odonto.exception.BusinessException;
import br.ufrgs.odonto.exception.ErrorCode;
import br.ufrgs.odonto.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request,
            Locale locale
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildResponse(HttpStatus.NOT_FOUND, ex.getErrorCode(), ex.getArgs(), request, locale));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(
            BusinessException ex,
            HttpServletRequest request,
            Locale locale
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildResponse(HttpStatus.CONFLICT, ex.getErrorCode(), ex.getArgs(), request, locale));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request,
            Locale locale
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildResponse(HttpStatus.FORBIDDEN, ErrorCode.ERROR_ACCESS_DENIED, new Object[0], request, locale));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(
            AuthenticationException ex,
            HttpServletRequest request,
            Locale locale
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildResponse(HttpStatus.UNAUTHORIZED, ErrorCode.ERROR_AUTH_INVALID_CREDENTIALS, new Object[0], request, locale));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request,
            Locale locale
    ) {
        Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ValidationErrorResponse(
                        ErrorCode.ERROR_VALIDATION.name(),
                        HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        resolveMessage(ErrorCode.ERROR_VALIDATION, new Object[0], locale),
                        request.getRequestURI(),
                        traceId(),
                        fields
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpServletRequest request, Locale locale) {
        var status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(buildResponse(status, ErrorCode.ERROR_VALIDATION, new Object[0], request, locale));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request, Locale locale) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.ERROR_INTERNAL_SERVER, new Object[0], request, locale));
    }

    private ApiErrorResponse buildResponse(
            HttpStatus status,
            ErrorCode errorCode,
            Object[] args,
            HttpServletRequest request,
            Locale locale
    ) {
        return new ApiErrorResponse(
                errorCode.name(),
                status.value(),
                resolveMessage(errorCode, args, locale),
                request.getRequestURI(),
                traceId()
        );
    }

    private String resolveMessage(ErrorCode errorCode, Object[] args, Locale locale) {
        return messageSource.getMessage(errorCode.getMessageKey(), args, errorCode.name(), locale);
    }

    private String traceId() {
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : UUID.randomUUID().toString();
    }

    public record ApiErrorResponse(String code, int status, String message, String path, String traceId) {
        public OffsetDateTime timestamp() {
            return OffsetDateTime.now();
        }
    }

    public record ValidationErrorResponse(
            String code,
            int status,
            String message,
            String path,
            String traceId,
            Map<String, String> fields
    ) {
        public OffsetDateTime timestamp() {
            return OffsetDateTime.now();
        }
    }
}
