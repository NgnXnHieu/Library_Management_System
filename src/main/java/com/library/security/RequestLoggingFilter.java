package com.library.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter ghi log tự động cho mọi HTTP Request và Response đi qua hệ thống.
 * Kế thừa OncePerRequestFilter đảm bảo chỉ thực thi duy nhất 1 lần cho mỗi request.
 * Đặt trước JwtAuthenticationFilter để đo toàn bộ thời gian thực thi (Latency)
 * và ghi nhận mã trạng thái HTTP, địa chỉ IP và danh tính người gọi.
 */
@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    /**
     * Bỏ qua các request tĩnh hoặc tài liệu Swagger/OpenAPI để tránh làm rác log.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v3/api-docs")
                || path.startsWith("/api/swagger-ui")
                || path.endsWith(".ico")
                || path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".png")
                || path.endsWith(".jpg");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // =========================================================================
        // BƯỚC 1: BẮT ĐẦU BẤM GIỜ KHI REQUEST BẮT ĐẦU VÀO HỆ THỐNG
        // =========================================================================
        long startTime = System.currentTimeMillis();

        try {
            // =====================================================================
            // BƯỚC 2: CHUYỂN TIẾP CHO CÁC FILTER TIẾP THEO (JWT, CONTROLLER, DB...)
            // =====================================================================
            filterChain.doFilter(request, response);
        } finally {
            // =====================================================================
            // BƯỚC 3: KHI CONTROLLER & CÁC FILTER ĐÃ XỬ LÝ XONG VÀ ĐANG TRẢ VỀ
            // =====================================================================
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            // Trích xuất phương thức HTTP (GET, POST, PUT, DELETE...)
            String method = request.getMethod();

            // Trích xuất URL kèm tham số tìm kiếm (Query String nếu có)
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            String fullUrl = StringUtils.hasText(queryString) ? uri + "?" + queryString : uri;

            // Lấy địa chỉ Client IP thực tế
            String clientIp = getClientIp(request);

            // Lấy tên người dùng hiện tại (nếu đã xác thực qua JWT)
            String user = getCurrentUser();

            // =====================================================================
            // BƯỚC 4: GHI LOG PHÂN CẤP THEO MÃ HTTP STATUS
            // =====================================================================
            if (status >= 500) {
                // Lỗi máy chủ (Server Error 5xx)
                log.error("[HTTP REQUEST] [{}] {} | Status: {} | Duration: {}ms | IP: {} | User: {}",
                        method, fullUrl, status, duration, clientIp, user);
            } else if (status >= 400) {
                // Lỗi client (Bad Request, Unauthorized, Forbidden, Not Found 4xx)
                log.warn("[HTTP REQUEST] [{}] {} | Status: {} | Duration: {}ms | IP: {} | User: {}",
                        method, fullUrl, status, duration, clientIp, user);
            } else {
                // Thành công (2xx, 3xx)
                log.info("[HTTP REQUEST] [{}] {} | Status: {} | Duration: {}ms | IP: {} | User: {}",
                        method, fullUrl, status, duration, clientIp, user);
            }
        }
    }

    /**
     * Trích xuất địa chỉ IP thực tế của client kể cả khi ứng dụng chạy phía sau Proxy, Nginx hay Load Balancer.
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // Trường hợp đi qua nhiều tầng proxy, header X-Forwarded-For chứa danh sách IP cách nhau bởi dấu phẩy
        if (StringUtils.hasText(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // Chuẩn hóa địa chỉ localhost IPv6 thành IPv4 cho dễ nhìn
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return ip;
    }

    /**
     * Lấy Username của người dùng đang gọi API từ SecurityContextHolder.
     * Nếu là request công khai chưa đăng nhập, trả về "Anonymous".
     */
    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "Anonymous";
    }
}
