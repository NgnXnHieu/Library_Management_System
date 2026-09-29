package com.library.util;

import com.library.security.UserDetailCustom;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Lớp tiện ích hỗ trợ trích xuất thông tin người dùng đang đăng nhập từ SecurityContextHolder.
 * Được thiết kế theo chuẩn Utility Class (final class, private constructor, toàn bộ static methods).
 */
public final class SecurityUtil {

    private SecurityUtil() {
        // Ngăn chặn việc khởi tạo instance của Utility class
    }

    /**
     * Lấy đối tượng UserDetailCustom của người dùng hiện tại từ SecurityContextHolder.
     *
     * @return Optional chứa UserDetailCustom nếu người dùng đã đăng nhập, ngược lại trả về Optional rỗng.
     */
    public static Optional<UserDetailCustom> getCurrentUser() {
        // Bước 1: Lấy thông tin Authentication hiện tại từ SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Bước 2: Kiểm tra Authentication tồn tại và đã được xác thực thành công
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        // Bước 3: Kiểm tra Principal có đúng kiểu UserDetailCustom không (loại trừ AnonymousAuthenticationToken)
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailCustom userDetails) {
            return Optional.of(userDetails);
        }

        return Optional.empty();
    }

    /**
     * Lấy thông tin UserDetailCustom của người dùng hiện tại.
     * Ném ra ngoại lệ AuthenticationCredentialsNotFoundException nếu chưa đăng nhập.
     * Thích hợp dùng trong các logic nghiệp vụ bắt buộc phải có phiên đăng nhập của người dùng.
     *
     * @return UserDetailCustom của người dùng hiện tại
     * @throws AuthenticationCredentialsNotFoundException nếu chưa đăng nhập
     */
    public static UserDetailCustom getCurrentUserOrThrow() {
        return getCurrentUser().orElseThrow(() ->
                new AuthenticationCredentialsNotFoundException("Người dùng chưa đăng nhập hoặc phiên làm việc đã hết hạn!"));
    }

    /**
     * Lấy nhanh Account ID (ID tài khoản trong bảng accounts) của người dùng hiện tại.
     *
     * @return Optional chứa Account ID
     */
    public static Optional<Long> getCurrentAccountId() {
        return getCurrentUser().map(UserDetailCustom::getAccountId);
    }

    /**
     * Lấy bắt buộc Account ID của người dùng hiện tại. Nếu chưa đăng nhập sẽ ném ngoại lệ.
     *
     * @return Account ID
     * @throws AuthenticationCredentialsNotFoundException nếu chưa đăng nhập
     */
    public static Long getRequiredAccountId() {
        return getCurrentUserOrThrow().getAccountId();
    }

    /**
     * Lấy nhanh User ID (ID thông tin cá nhân trong bảng users) của người dùng hiện tại.
     *
     * @return Optional chứa User ID
     */
    public static Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(UserDetailCustom::getUserId);
    }

    /**
     * Lấy bắt buộc User ID của người dùng hiện tại. Nếu chưa đăng nhập sẽ ném ngoại lệ.
     *
     * @return User ID
     * @throws AuthenticationCredentialsNotFoundException nếu chưa đăng nhập
     */
    public static Long getRequiredUserId() {
        return getCurrentUserOrThrow().getUserId();
    }

    /**
     * Lấy nhanh Branch ID (ID chi nhánh làm việc/đọc sách) của người dùng hiện tại.
     *
     * @return Optional chứa Branch ID (có thể rỗng nếu user không thuộc chi nhánh nào)
     */
    public static Optional<Long> getCurrentBranchId() {
        return getCurrentUser().map(UserDetailCustom::getBranchId);
    }

    /**
     * Lấy nhanh Role Code (Mã vai trò, ví dụ: ADMIN, LIBRARIAN, READER) của người dùng hiện tại.
     *
     * @return Optional chứa Role Code
     */
    public static Optional<String> getCurrentRoleCode() {
        return getCurrentUser().map(UserDetailCustom::getRoleCode);
    }

    /**
     * Lấy nhanh Username đăng nhập của người dùng hiện tại.
     *
     * @return Optional chứa Username
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUser().map(UserDetailCustom::getUsername);
    }

    /**
     * Kiểm tra xem request hiện tại có đang được xác thực hợp lệ hay không.
     *
     * @return true nếu đã đăng nhập và có thông tin UserDetailCustom, ngược lại là false
     */
    public static boolean isAuthenticated() {
        return getCurrentUser().isPresent();
    }
}
