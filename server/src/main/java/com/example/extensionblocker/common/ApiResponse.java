package com.example.extensionblocker.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 응답의 표준 형식을 정의하는 클래스.
 * 모든 API 응답은 이 형식으로 래핑됩니다.
 *
 * @param <T> 응답 데이터의 타입
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    /**
     * 요청 성공 여부.
     */
    private boolean success;
    /**
     * 실제 응답 데이터.
     */
    private T data;
    /**
     * 오류 발생 시 오류 정보.
     */
    private ErrorResponse error;

    /**
     * 데이터와 함께 성공 응답을 생성합니다.
     *
     * @param data 응답할 실제 데이터
     * @param <T>  데이터의 타입
     * @return 성공적인 API 응답 객체
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * 데이터 없이 성공 응답을 생성합니다.
     *
     * @param <T> 응답 데이터의 타입 (제네릭 타입 유지를 위함)
     * @return 성공적인 API 응답 객체
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, null, null);
    }

    /**
     * 오류 코드와 메시지를 포함하는 실패 응답을 생성합니다.
     *
     * @param errorCode 오류를 식별하는 코드
     * @param message   오류에 대한 설명 메시지
     * @param <T>       응답 데이터의 타입 (제네릭 타입 유지를 위함)
     * @return 실패적인 API 응답 객체
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(false, null, new ErrorResponse(errorCode, message));
    }

    public static <T> ApiResponse<T> error(String errorCode, String message, T data) {
        return new ApiResponse<>(false, data, new ErrorResponse(errorCode, message));
    }

    /**
     * API 오류 정보를 나타내는 내부 클래스.
     */
    @Getter
    @AllArgsConstructor
    public static class ErrorResponse {
        /**
         * 오류를 식별하는 코드.
         */
        private String code;
        /**
         * 오류에 대한 설명 메시지.
         */
        private String message;
    }
}
