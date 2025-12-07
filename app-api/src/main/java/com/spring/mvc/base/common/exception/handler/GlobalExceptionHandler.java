package com.spring.mvc.base.common.exception.handler;

import com.spring.mvc.base.common.dto.api.FieldError;
import com.spring.mvc.base.common.dto.api.ErrorResponse;
import com.spring.mvc.base.common.exception.CustomException;
import com.spring.mvc.base.common.exception.ErrorCode;
import com.spring.mvc.base.common.exception.http.ConflictException;
import com.spring.mvc.base.common.exception.http.ForbiddenException;
import com.spring.mvc.base.common.exception.http.InvalidRequestException;
import com.spring.mvc.base.common.exception.http.NotFoundException;
import com.spring.mvc.base.common.exception.http.UnauthorizedException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("[CustomException] {} - {}", errorCode.name(), e.getMessage(), e);
        ErrorResponse response = ErrorResponse.from(errorCode);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("[NotFoundException] {} - {}", errorCode.name(), e.getMessage(), e);
        ErrorResponse response = ErrorResponse.from(errorCode);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("[UnauthorizedException] {} - {}", errorCode.name(), e.getMessage(), e);
        ErrorResponse response = ErrorResponse.from(errorCode);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("[ForbiddenException] {} - {}", errorCode.name(), e.getMessage(), e);
        ErrorResponse response = ErrorResponse.from(errorCode);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("[ConflictException] {} - {}", errorCode.name(), e.getMessage(), e);
        ErrorResponse response = ErrorResponse.from(errorCode);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(InvalidRequestException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("[InvalidRequestException] {} - {}", errorCode.name(), e.getMessage(), e);
        ErrorResponse response = ErrorResponse.from(errorCode);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ErrorResponse> handleValidationException(BindException e) {
        log.error("[ValidationException] {}", e.getMessage(), e);

        List<FieldError> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> FieldError.of(
                        fieldError.getField(),
                        fieldError.getDefaultMessage(),
                        fieldError.getRejectedValue()
                ))
                .toList();

        ErrorResponse response = ErrorResponse.of("validation_failed", errors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwtException(ExpiredJwtException e) {
        log.error("[ExpiredJwtException] {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of("토큰이 만료되었습니다");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(JwtException e) {
        log.error("[JwtException] {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of("유효하지 않은 토큰입니다");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("[UnhandledException] {}", e.getMessage(), e);
        ErrorResponse response = ErrorResponse.of("internal_server_error");
        return ResponseEntity.internalServerError().body(response);
    }
}
