package com.library.exception;

import lombok.Getter;

/**
 * Ngoại lệ nghiệp vụ tùy chỉnh (Custom Business Exception) sử dụng ErrorCode.
 * Tự động mang theo mã lỗi, thông điệp lỗi và trạng thái HTTP để GlobalExceptionHandler xử lý.
 */
@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * Khởi tạo ngoại lệ với thông điệp mặc định từ ErrorCode.
     *
     * @param errorCode Mã lỗi định nghĩa sẵn
     */
    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * Khởi tạo ngoại lệ với thông điệp có truyền tham số động (format %s).
     *
     * @param errorCode Mã lỗi định nghĩa sẵn
     * @param args Các tham số truyền vào template message của ErrorCode
     */
    public AppException(ErrorCode errorCode, Object... args) {
        super(String.format(errorCode.getMessage(), args));
        this.errorCode = errorCode;
    }
}
