package com.example.extensionblocker.exception;

import com.example.extensionblocker.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 핸들러.
 * 애플리케이션 전반에서 발생하는 예외를 중앙 집중식으로 처리하여 일관된 API 응답을 제공합니다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * {@link IllegalArgumentException} 발생 시 처리합니다.
     * 주로 잘못된 인자 값으로 인한 예외를 BadRequest (400) 응답으로 변환합니다.
     *
     * @param ex 발생한 IllegalArgumentException
     * @return BAD_REQUEST 상태 코드와 오류 메시지를 포함하는 ApiResponse
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(ApiResponse.error("BAD_REQUEST", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * {@link IllegalStateException} 발생 시 처리합니다.
     * 주로 애플리케이션의 상태가 유효하지 않을 때 발생하는 예외를 Conflict (409) 응답으로 변환합니다.
     * (예: 커스텀 확장자 개수 제한 초과)
     *
     * @param ex 발생한 IllegalStateException
     * @return CONFLICT 상태 코드와 오류 메시지를 포함하는 ApiResponse
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
        return new ResponseEntity<>(ApiResponse.error("CONFLICT", ex.getMessage()), HttpStatus.CONFLICT); // 개수 제한 초과 등의 경우
    }

    /**
     * 기타 예상치 못한 모든 예외를 처리합니다.
     * InternalServerError (500) 응답으로 변환합니다.
     *
     * @param ex 발생한 일반 예외
     * @return INTERNAL_SERVER_ERROR 상태 코드와 오류 메시지를 포함하는 ApiResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        return new ResponseEntity<>(ApiResponse.error("INTERNAL_SERVER_ERROR", "An unexpected error occurred: " + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
