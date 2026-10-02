package com.library.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enum định nghĩa danh sách toàn bộ các mã lỗi tập trung trong hệ thống.
 * Mỗi mã lỗi bao gồm: Mã định danh (code), Thông điệp mặc định (message) và HTTP Status tương ứng.
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // =========================================================================
    // 1. CÁC MÃ LỖI HỆ THỐNG & XÁC THỰC BẢO MẬT (0000 - 0999)
    // =========================================================================
    UNCATEGORIZED_EXCEPTION("UNCATEGORIZED_EXCEPTION", "Đã xảy ra lỗi hệ thống ngoài ý muốn!", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED("UNAUTHORIZED", "Bạn chưa đăng nhập hoặc phiên làm việc đã hết hạn!", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("ACCESS_DENIED", "Bạn không có quyền thực hiện thao tác này! Chỉ ADMIN mới có quyền truy cập.", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Tài khoản hoặc mật khẩu không chính xác!", HttpStatus.BAD_REQUEST),
    ACCOUNT_INACTIVE_OR_LOCKED("ACCOUNT_INACTIVE_OR_LOCKED", "Tài khoản của bạn đã bị khóa hoặc chưa được kích hoạt!", HttpStatus.BAD_REQUEST),

    // =========================================================================
    // 2. CÁC MÃ LỖI TÀI KHOẢN (ACCOUNT) & NGƯỜI DÙNG (USER) (1000 - 1999)
    // =========================================================================
    ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND", "Không tìm thấy tài khoản trên hệ thống!", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_FOUND_BY_ID("ACCOUNT_NOT_FOUND_BY_ID", "Không tìm thấy tài khoản với ID: %s", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_FOUND_BY_USERNAME("ACCOUNT_NOT_FOUND_BY_USERNAME", "Không tìm thấy tài khoản với tên đăng nhập: %s", HttpStatus.NOT_FOUND),
    ACCOUNT_ALREADY_EXISTS("ACCOUNT_ALREADY_EXISTS", "Tài khoản '%s' đã tồn tại trên hệ thống!", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "Email '%s' đã được đăng ký trên hệ thống!", HttpStatus.BAD_REQUEST),
    PHONE_ALREADY_EXISTS("PHONE_ALREADY_EXISTS", "Số điện thoại '%s' đã được đăng ký trên hệ thống!", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("USER_NOT_FOUND", "Không tìm thấy thông tin người dùng!", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND_BY_ID("USER_NOT_FOUND_BY_ID", "Không tìm thấy người dùng với ID: %s", HttpStatus.NOT_FOUND),

    // =========================================================================
    // 3. CÁC MÃ LỖI VAI TRÒ (ROLE) (2000 - 2999)
    // =========================================================================
    ROLE_NOT_FOUND("ROLE_NOT_FOUND", "Không tìm thấy vai trò với mã: %s", HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND_BY_ID("ROLE_NOT_FOUND_BY_ID", "Không tìm thấy vai trò với ID: %s", HttpStatus.NOT_FOUND),
    ROLE_ALREADY_EXISTS("ROLE_ALREADY_EXISTS", "Mã vai trò '%s' đã tồn tại trên hệ thống!", HttpStatus.BAD_REQUEST),

    // =========================================================================
    // 4. CÁC MÃ LỖI CHI NHÁNH (BRANCH) (3000 - 3999)
    // =========================================================================
    BRANCH_NOT_FOUND("BRANCH_NOT_FOUND", "Không tìm thấy chi nhánh với ID: %s", HttpStatus.NOT_FOUND),
    BRANCH_ALREADY_EXISTS("BRANCH_ALREADY_EXISTS", "Chi nhánh với mã '%s' đã tồn tại!", HttpStatus.BAD_REQUEST),
    BRANCH_CANNOT_DELETE("BRANCH_CANNOT_DELETE", "Không thể xóa chi nhánh '%s' vì đang có người dùng thuộc chi nhánh này!", HttpStatus.BAD_REQUEST),

    // =========================================================================
    // 5. CÁC MÃ LỖI THỂ LOẠI (CATEGORY) (4000 - 4999)
    // =========================================================================
    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "Không tìm thấy thể loại với ID: %s", HttpStatus.NOT_FOUND),
    CATEGORY_ALREADY_EXISTS("CATEGORY_ALREADY_EXISTS", "Thể loại với tên '%s' đã tồn tại!", HttpStatus.BAD_REQUEST),
    CATEGORY_CANNOT_DELETE("CATEGORY_CANNOT_DELETE", "Không thể xóa thể loại '%s' vì đang có sách thuộc thể loại này!", HttpStatus.BAD_REQUEST),

    // =========================================================================
    // 6. CÁC MÃ LỖI SÁCH (BOOK) & TẬP TIN (FILE) (5000 - 5999)
    // =========================================================================
    BOOK_NOT_FOUND("BOOK_NOT_FOUND", "Không tìm thấy sách với ID: %s", HttpStatus.NOT_FOUND),
    BOOK_ALREADY_EXISTS("BOOK_ALREADY_EXISTS", "Mã ISBN '%s' đã tồn tại trên hệ thống!", HttpStatus.BAD_REQUEST),
    BOOK_CANNOT_DELETE("BOOK_CANNOT_DELETE", "Không thể xóa sách '%s' vì đã phát sinh giao dịch mượn sách!", HttpStatus.BAD_REQUEST),
    INVALID_FILE("INVALID_FILE", "%s", HttpStatus.BAD_REQUEST);

    /**
     * Mã lỗi định danh (dùng để trả về cho Frontend/Client phân biệt)
     */
    private final String code;

    /**
     * Thông điệp lỗi mặc định bằng tiếng Việt (hỗ trợ định dạng chuỗi %s)
     */
    private final String message;

    /**
     * Mã trạng thái HTTP trả về
     */
    private final HttpStatus httpStatus;
}
