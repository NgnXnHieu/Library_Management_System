package com.library.security;

import com.library.service.impl.JwtServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtServiceImpl jwtService;

    public JwtAuthenticationFilter(@Autowired(required = false) JwtServiceImpl jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (jwtService == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = extractToken(request);

            if (StringUtils.hasText(token) && jwtService.isTokenValid(token) && jwtService.isAccessToken(token)) {
                Long accountId = jwtService.extractAccountId(token);

                if (accountId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetailCustom userDetails = jwtService.getUserDetailsByAccountId(accountId);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Đã xác thực thành công cho tài khoản ID: {}, Role: {}", accountId, userDetails.getRoleCode());
                }
            }
        } catch (Exception ex) {
            log.error("Không thể xác thực người dùng qua JWT: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Trích xuất token từ Cookie (ưu tiên) hoặc Header Authorization
     */
    private String extractToken(HttpServletRequest request) {
        // 1. Kiểm tra Cookie "accessToken"
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SecurityConstants.ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
        }

        // 2. Fallback: Kiểm tra Header "Authorization: Bearer <token>"
        String authHeader = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return authHeader.substring(SecurityConstants.BEARER_PREFIX.length()).trim();
        }

        return null;
    }
}
