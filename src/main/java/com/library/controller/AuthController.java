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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtServiceImpl jwtService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> register(@Valid @RequestBody RegisterRequestForm requestForm) {
        UserResponseDto responseDto = authService.register(requestForm);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký tài khoản thành công!", responseDto));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestForm requestForm) {
        LoginResponseDto responseDto = authService.login(requestForm);

        // Set cookies với thời gian sống trùng với expiration của token
        ResponseCookie accessCookie = jwtService.createAccessTokenCookie(responseDto.getAccessToken());
        ResponseCookie refreshCookie = jwtService.createRefreshTokenCookie(responseDto.getRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success("Đăng nhập thành công!", null));
    }

    /**
     * API Đăng xuất:
     * - Gọi AuthService để xóa token và refreshToken trong DB của account hiện tại.
     * - Tạo response xóa Cookie accessToken và refreshToken trên trình duyệt.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // Bước 1: Gọi xuống tầng Service để xử lý nghiệp vụ xóa token trong DB và dọn SecurityContext
        authService.logout();

        // Bước 2: Tạo cookie rỗng với maxAge = 0 để xóa cookie phía client/browser
        ResponseCookie cleanAccessCookie = jwtService.createCleanAccessTokenCookie();
        ResponseCookie cleanRefreshCookie = jwtService.createCleanRefreshTokenCookie();

        // Bước 3: Đính kèm header Set-Cookie để client xóa cookie và trả về thông báo thành công
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanAccessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, cleanRefreshCookie.toString())
                .body(ApiResponse.success("Đăng xuất thành công!", null));
    }
}
