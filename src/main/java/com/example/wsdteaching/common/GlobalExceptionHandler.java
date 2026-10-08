package com.example.wsdteaching.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleApiException(ApiException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(ApiResponse.error(new ApiError(exception.getStatus().name(), exception.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ApiError>> handleUnexpectedException(Exception exception) {
        log.error("요청 처리 중 예상하지 못한 오류가 발생했습니다.", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(new ApiError("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.")));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        Map<String, String> details = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                details.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return super.handleExceptionInternal(exception,
                ApiResponse.error(new ApiError("BAD_REQUEST", "입력값을 확인해주세요.", details)),
                headers, status, request);
    }

    // Also standardize framework errors: invalid JSON, invalid IDs, 404, 405 and 415.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        HttpStatus httpStatus = HttpStatus.resolve(status.value());
        String code = httpStatus == null ? "HTTP_" + status.value() : httpStatus.name();
        String message = switch (status.value()) {
            case 400 -> "요청 형식 또는 입력값이 올바르지 않습니다.";
            case 404 -> "요청한 경로를 찾을 수 없습니다.";
            case 405 -> "해당 경로에서 지원하지 않는 HTTP 메소드입니다.";
            case 415 -> "Content-Type을 application/json으로 지정해주세요.";
            default -> "요청을 처리할 수 없습니다.";
        };
        return super.handleExceptionInternal(exception,
                ApiResponse.error(new ApiError(code, message)), headers, status, request);
    }
}
