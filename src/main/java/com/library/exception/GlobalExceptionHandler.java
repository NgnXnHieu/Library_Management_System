package com.library.exception;

import com.library.dto.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================================
    // 1. CÁC EXCEPTION BẢO MẬT & XÁC THỰC (SPRING SECURITY)
    // =========================================================================

    /**
     * Bắt lỗi 401: Chưa đăng nhập, Token thiếu, hết hạn hoặc không hợp lệ
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Lỗi xác thực (AuthenticationException): {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.UNAUTHORIZED.value(),
                "Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn!",
                "UNAUTHORIZED"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Bắt lỗi 401: Sai Username hoặc Password khi đăng nhập
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Sai thông tin đăng nhập: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.UNAUTHORIZED.value(),
                "Tên đăng nhập hoặc mật khẩu không chính xác!",
                "BAD_CREDENTIALS"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Bắt lỗi 403: Đã xác thực nhưng không đủ quyền/role để truy cập tài nguyên
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Truy cập bị từ chối (AccessDeniedException): {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.FORBIDDEN.value(),
                "Bạn không có quyền truy cập tài nguyên này!",
                "FORBIDDEN"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // =========================================================================
    // 2. CÁC EXCEPTION DO DỰ ÁN TỰ ĐỊNH NGHĨA (CUSTOM BUSINESS EXCEPTIONS)
    // =========================================================================

    /**
     * Bắt lỗi nghiệp vụ tập trung AppException (tự động lấy mã lỗi, thông điệp và HttpStatus tương ứng)
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ApiResponse<Void> response = ApiResponse.error(
                errorCode.getHttpStatus().value(),
                ex.getMessage(),
                errorCode.getCode()
        );
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /**
     * Bắt lỗi 404: Không tìm thấy tài nguyên theo ID, Code,...
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                "RESOURCE_NOT_FOUND"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Bắt lỗi 400: Nghiệp vụ không hợp lệ (trùng lặp dữ liệu, tài khoản bị khóa,...)
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequestException(BadRequestException ex) {
        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                "BAD_REQUEST"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // =========================================================================
    // 3. CÁC EXCEPTION VALIDATION DỮ LIỆU ĐẦU VÀO
    // =========================================================================

    /**
     * Bắt lỗi validate @Valid trên @RequestBody (DTO, Form)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Không hợp lệ",
                        (msg1, msg2) -> msg1 + ", " + msg2,
                        LinkedHashMap::new
                ));

        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "Dữ liệu đầu vào không hợp lệ",
                "VALIDATION_ERROR",
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bắt lỗi validate @Validated trên @RequestParam hoặc @PathVariable
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> fieldErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> {
                            String path = violation.getPropertyPath().toString();
                            return path.substring(path.lastIndexOf('.') + 1);
                        },
                        ConstraintViolation::getMessage,
                        (msg1, msg2) -> msg1 + ", " + msg2,
                        LinkedHashMap::new
                ));

        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "Tham số đầu vào không thỏa mãn điều kiện ràng buộc!",
                "VALIDATION_ERROR",
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // =========================================================================
    // 4. CÁC EXCEPTION THAM SỐ VÀ GIAO THỨC HTTP
    // =========================================================================

    /**
     * Bắt lỗi sai kiểu dữ liệu tham số trên URL (VD: truyền "abc" vào trường số Long)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();
        String expectedType = (ex.getRequiredType() != null) ? ex.getRequiredType().getSimpleName() : "Không xác định";
        String providedValue = (ex.getValue() != null) ? ex.getValue().toString() : "null";

        String message = String.format("Tham số '%s' nhận giá trị '%s' không hợp lệ. Vui lòng truyền kiểu dữ liệu '%s'!",
                paramName, providedValue, expectedType);

        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                message,
                "TYPE_MISMATCH"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bắt lỗi thiếu tham số bắt buộc @RequestParam trên URL
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        String message = String.format("Thiếu tham số bắt buộc '%s' (kiểu %s) trên URL!",
                ex.getParameterName(), ex.getParameterType());

        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                message,
                "MISSING_PARAMETER"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bắt lỗi Body rỗng hoặc cú pháp JSON gửi lên sai định dạng
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "Dữ liệu gửi lên bị thiếu hoặc định dạng JSON không hợp lệ. Vui lòng kiểm tra lại!",
                "MALFORMED_JSON"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Bắt lỗi gọi sai HTTP Method (VD: Endpoint chỉ hỗ trợ POST nhưng gửi GET)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String unsupportedMethod = ex.getMethod();
        String supportedMethods = (ex.getSupportedHttpMethods() != null)
                ? ex.getSupportedHttpMethods().stream().map(HttpMethod::name).collect(Collectors.joining(", "))
                : "Không xác định";

        String message = String.format("Đường dẫn này không hỗ trợ phương thức '%s'. Các phương thức được hỗ trợ: %s.",
                unsupportedMethod, supportedMethods);

        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                message,
                "METHOD_NOT_SUPPORTED"
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * Bắt lỗi định dạng Content-Type không được hỗ trợ (VD: gửi text/plain thay vì application/json)
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        String unsupportedType = (ex.getContentType() != null) ? ex.getContentType().toString() : "Không xác định";
        String supportedTypes = ex.getSupportedMediaTypes().stream()
                .map(MediaType::toString)
                .collect(Collectors.joining(", "));

        String message = String.format("Định dạng dữ liệu '%s' không được hỗ trợ. Vui lòng gửi dữ liệu dưới định dạng: %s.",
                unsupportedType, supportedTypes);

        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                message,
                "UNSUPPORTED_MEDIA_TYPE"
        );
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    /**
     * Bắt lỗi đường dẫn không tồn tại trong Spring Boot 3 (404 Not Found)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException ex) {
        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.NOT_FOUND.value(),
                "Đường dẫn '" + ex.getResourcePath() + "' không tồn tại trên hệ thống!",
                "NOT_FOUND"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Bắt lỗi xung đột dữ liệu Optimistic Locking (khi có 2 phiên cùng sửa 1 bản ghi)
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex) {
        log.warn("Xung đột khóa lạc quan (Optimistic Locking): {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.CONFLICT.value(),
                "Dữ liệu đã bị thay đổi bởi thao tác khác. Vui lòng tải lại và thử lại!",
                "OPTIMISTIC_LOCK_CONFLICT"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Bắt lỗi NullPointerException
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Void>> handleNullPointerException(NullPointerException ex) {
        log.error("Lỗi NullPointerException: ", ex);
        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Hệ thống gặp sự cố xử lý dữ liệu (Null Pointer).",
                "INTERNAL_SERVER_ERROR"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // =========================================================================
    // 5. CATCH-ALL CHO CÁC EXCEPTION KHÔNG MONG MUỐN CÒN LẠI
    // =========================================================================

    /**
     * Bắt tất cả các lỗi chưa được định nghĩa cụ thể phía trên (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("Lỗi không xác định (Internal Server Error): ", ex);
        ApiResponse<Void> response = ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage() != null ? ex.getMessage() : "Đã xảy ra lỗi hệ thống ngoài ý muốn!",
                "INTERNAL_SERVER_ERROR"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
