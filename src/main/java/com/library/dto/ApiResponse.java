package com.library.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private Integer status;
    private String message;
    private T data;
    private String errorCode;
    private Object errors;

    @Builder.Default
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(status)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(HttpStatus.BAD_REQUEST.value(), message, null, null);
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return error(HttpStatus.BAD_REQUEST.value(), message, errorCode, null);
    }

    public static <T> ApiResponse<T> error(int status, String message, String errorCode) {
        return error(status, message, errorCode, null);
    }

    public static <T> ApiResponse<T> error(int status, String message, String errorCode, Object errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .errorCode(errorCode)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

