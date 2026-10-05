package com.library.security;

import com.library.service.impl.JwtServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter chặn mọi HTTP request để kiểm tra và xác thực JWT token.
 * Kế thừa OncePerRequestFilter đảm bảo filter chỉ thực thi duy nhất 1 lần cho
 * mỗi request.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtServiceImpl jwtService;

    /**
     * Dùng Constructor Injection để tiêm phụ thuộc JwtServiceImpl
     */
    public JwtAuthenticationFilter(JwtServiceImpl jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // =========================================================================
            // BƯỚC 1: LẤY VÀ KIỂM TRA COOKIES ĐƯỢC GỬI LÊN
            // =========================================================================
            String token = extractTokenFromCookie(request);

            // Fallback: Nếu không tìm thấy trong Cookie, hỗ trợ kiểm tra thêm Authorization
            // Header (Bearer token)
            if (!StringUtils.hasText(token)) {
                token = extractTokenFromHeader(request);
            }

            // =========================================================================
            // BƯỚC 2: KIỂM TRA TÍNH HỢP LỆ CỦA TOKEN
            // =========================================================================
            // Token phải: có nội dung, chữ ký hợp lệ/chưa hết hạn, và phải là loại ACCESS
            // token
            if (StringUtils.hasText(token) && jwtService.isTokenValid(token) && jwtService.isAccessToken(token)) {

                // Trích xuất accountId được mã hóa bên trong claims của token
                Long accountId = jwtService.extractAccountId(token);

                // =========================================================================
                // BƯỚC 3: KIỂM TRA TÀI KHOẢN, USER VÀ NẠP THÔNG TIN
                // =========================================================================
                // Chỉ xử lý nếu có accountId và SecurityContext hiện tại chưa được xác thực
                if (accountId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Gọi hàm getUserDetailsByAccountId để:
                    // 1. Kiểm tra tài khoản (Account) tồn tại trong DB và ở trạng thái ACTIVE
                    // 2. So sánh chuỗi token hiện tại với token lưu trong DB (đảm bảo token chưa bị
                    // đăng xuất/ghi đè)
                    // 3. Kiểm tra thông tin người dùng (User) liên kết tồn tại và ở trạng thái
                    // ACTIVE
                    // 4. Lấy thông tin Role, Branch, FullName và đóng gói thành UserDetailCustom
                    UserDetailCustom userDetails = jwtService.getUserDetailsByAccountId(accountId, token);

                    // =========================================================================
                    // BƯỚC 4: TẠO AUTHENTICATION VÀ GÁN VÀO SECURITY CONTEXT HOLDER
                    // =========================================================================
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Không cần lưu credentials/password vào SecurityContext
                            userDetails.getAuthorities() // Danh sách quyền hạn (ROLE_...)
                    );

                    // Gán thêm chi tiết môi trường request (IP, Session ID, v.v.)
                    // authentication.setDetails(new
                    // WebAuthenticationDetailsSource().buildDetails(request));

                    // Lưu đối tượng Authentication vào SecurityContextHolder để các tầng sau sử
                    // dụng
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Xác thực thành công cho tài khoản ID: [{}], Username: [{}], Role: [{}]",
                            accountId, userDetails.getUsername(), userDetails.getRoleCode());
                }
            }
        } catch (Exception ex) {
            // Khi có lỗi xảy ra (token giả mạo, hết hạn, tài khoản bị khóa/xóa...):
            // Ghi nhận log cảnh báo và xóa sạch SecurityContext để đảm bảo an toàn
            log.warn("Không thể xác thực danh tính người dùng: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        // =========================================================================
        // BƯỚC 5: LUÔN CHUYỂN TIẾP REQUEST CHO CÁC FILTER TIẾP THEO TRONG CHUỖI
        // =========================================================================
        filterChain.doFilter(request, response);
    }

    /**
     * Trích xuất JWT Token từ Cookies của request
     */
    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SecurityConstants.ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())
                        && StringUtils.hasText(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Trích xuất JWT Token từ Header 'Authorization: Bearer <token>' (phương án phụ
     * nếu không dùng Cookie)
     */
    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return authHeader.substring(SecurityConstants.BEARER_PREFIX.length()).trim();
        }
        return null;
    }
}
