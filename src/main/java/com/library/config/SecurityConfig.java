package com.library.config;

import com.library.security.JwtAuthenticationFilter;
import com.library.security.RequestLoggingFilter;
import com.library.security.SecurityConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RequestLoggingFilter requestLoggingFilter;
    private final HandlerExceptionResolver resolver;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RequestLoggingFilter requestLoggingFilter,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.requestLoggingFilter = requestLoggingFilter;
        this.resolver = resolver;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Bước 1: Các endpoint công khai (Public có tiền tố /public/**, Swagger, Actuator) không cần xác thực
                        .requestMatchers(SecurityConstants.PUBLIC_ENDPOINTS).permitAll()

                        // Bước 2: Phân quyền cấp cao cho ADMIN & BRANCHMANAGER (Khóa/Mở khóa tài khoản)
                        .requestMatchers(HttpMethod.PUT, SecurityConstants.ADMIN_AND_BM_PUT_ENDPOINTS)
                                .hasAnyRole("ADMIN", "BRANCHMANAGER")

                        // Bước 3: Phân quyền cho NHÂN SỰ TẠI CHI NHÁNH (BRANCHMANAGER & STAFF)
                        .requestMatchers(HttpMethod.POST, SecurityConstants.BRANCH_STAFF_POST_ENDPOINTS)
                                .hasAnyRole("BRANCHMANAGER", "STAFF")
                        .requestMatchers(HttpMethod.GET, SecurityConstants.BRANCH_STAFF_GET_ENDPOINTS)
                                .hasAnyRole("BRANCHMANAGER", "STAFF")
                        .requestMatchers(HttpMethod.PUT, SecurityConstants.BRANCH_STAFF_PUT_ENDPOINTS)
                                .hasAnyRole("BRANCHMANAGER", "STAFF")

                        // Bước 4: Phân quyền cho TOÀN BỘ NHÂN SỰ NỘI BỘ (ADMIN, BRANCHMANAGER & STAFF)
                        // Được đặt trước cấu hình wildcard ADMIN để các endpoint chi tiết như /admin/borrow-slips không bị chặn nhầm
                        .requestMatchers(HttpMethod.POST, SecurityConstants.INTERNAL_STAFF_POST_ENDPOINTS)
                                .hasAnyRole("ADMIN", "BRANCHMANAGER", "STAFF")
                        .requestMatchers(HttpMethod.GET, SecurityConstants.INTERNAL_STAFF_GET_ENDPOINTS)
                                .hasAnyRole("ADMIN", "BRANCHMANAGER", "STAFF")
                        .requestMatchers(HttpMethod.PUT, SecurityConstants.INTERNAL_STAFF_PUT_ENDPOINTS)
                                .hasAnyRole("ADMIN", "BRANCHMANAGER", "STAFF")

                        // Bước 5: Phân quyền dành riêng cho QUẢN TRỊ VIÊN HỆ THỐNG (ADMIN)
                        // Lưu ý: ADMIN_ANY_METHOD_ENDPOINTS chứa wildcard "/admin/**" nên đặt sau các endpoint nội bộ cụ thể
                        .requestMatchers(SecurityConstants.ADMIN_ANY_METHOD_ENDPOINTS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, SecurityConstants.ADMIN_POST_ENDPOINTS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, SecurityConstants.ADMIN_PUT_ENDPOINTS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, SecurityConstants.ADMIN_DELETE_ENDPOINTS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, SecurityConstants.ADMIN_GET_ENDPOINTS).hasRole("ADMIN")

                        // Bước 6: Toàn bộ các request còn lại BẮT BUỘC PHẢI ĐĂNG NHẬP
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                resolver.resolveException(request, response, null, authException))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                resolver.resolveException(request, response, null, accessDeniedException))
                )
                .addFilterBefore(requestLoggingFilter, LogoutFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Cấu hình CORS cho phép Frontend Angular (localhost:4200) gửi và nhận Cookies (allowCredentials: true).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of(HttpHeaders.SET_COOKIE));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
