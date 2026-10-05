package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.auth.LoginResponseDto;
import com.library.dto.user.UserResponseDto;
import com.library.requestform.account.LoginRequestForm;
import com.library.requestform.account.RegisterRequestForm;
import com.library.service.AuthService;
import com.library.service.impl.JwtServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtServiceImpl jwtService;

    // API đăng ký tài khoản công khai (tiền tố /public/auth)
    @PostMapping({"/public/auth/register", "/auth/register"})
    public ResponseEntity<ApiResponse<UserResponseDto>> register(@Valid @RequestBody RegisterRequestForm requestForm) {
        UserResponseDto responseDto = authService.register(requestForm);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký tài khoản thành công!", responseDto));
    }

    // API đăng nhập công khai (tiền tố /public/auth)
    @PostMapping({"/public/auth/login", "/auth/login"})
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestForm requestForm) {
        LoginResponseDto responseDto = authService.login(requestForm);

        // Set cookies với thời gian sống trùng với expiration của token
        ResponseCookie accessCookie = jwtService.createAccessTokenCookie(responseDto.getAccessToken());
        ResponseCookie refreshCookie = jwtService.createRefreshTokenCookie(responseDto.getRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success("Đăng nhập thành công!", responseDto));
    }

    /**
     * API Đăng xuất:
     * - Gọi AuthService để xóa token và refreshToken trong DB của account hiện tại.
     * - Tạo response xóa Cookie accessToken và refreshToken trên trình duyệt.
     */
    @PostMapping({"/auth/logout", "/logout"})
    public ResponseEntity<ApiResponse<Void>> logout() {
        // Bước 1: Gọi xuống tầng Service để xử lý nghiệp vụ xóa token trong DB và dọn
        // SecurityContext
        authService.logout();

        // Bước 2: Tạo cookie rỗng với maxAge = 0 để xóa cookie phía client/browser
        ResponseCookie cleanAccessCookie = jwtService.createCleanAccessTokenCookie();
        ResponseCookie cleanRefreshCookie = jwtService.createCleanRefreshTokenCookie();

        // Bước 3: Đính kèm header Set-Cookie để client xóa cookie và trả về thông báo
        // thành công
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanAccessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, cleanRefreshCookie.toString())
                .body(ApiResponse.success("Đăng xuất thành công!", null));
    }

    /**
     * API Làm mới Token (Refresh Token):
     * - Nhận refreshToken từ HttpOnly Cookie của Client
     * - Gọi AuthService để kiểm tra tính hợp lệ và cấp cặp accessToken + refreshToken mới
     * - Tạo 2 Cookie mới và gán vào header Set-Cookie trả về cho Frontend
     * - Trả về thông tin cơ bản của tài khoản (username, fullName, role)
     */
    @PostMapping({"/public/auth/refresh", "/auth/refresh"})
    public ResponseEntity<ApiResponse<LoginResponseDto>> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie) {

        // Bước 1: Gọi xuống tầng Service để kiểm tra refreshToken và tạo cặp token mới
        LoginResponseDto responseDto = authService.refreshToken(refreshTokenCookie);

        // Bước 2: Tạo HttpOnly Cookies mới cho accessToken và refreshToken
        ResponseCookie accessCookie = jwtService.createAccessTokenCookie(responseDto.getAccessToken());
        ResponseCookie refreshCookie = jwtService.createRefreshTokenCookie(responseDto.getRefreshToken());

        // Bước 3: Đính kèm Cookies mới vào header Set-Cookie và trả về thông tin người dùng
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success("Làm mới token thành công!", responseDto));
    }
}
