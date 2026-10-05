package com.library.service;

import com.library.dto.auth.LoginResponseDto;
import com.library.dto.user.UserResponseDto;
import com.library.requestform.account.LoginRequestForm;
import com.library.requestform.account.RegisterRequestForm;

public interface AuthService {

    UserResponseDto register(RegisterRequestForm form);

    LoginResponseDto login(LoginRequestForm form);

    /**
     * Đăng xuất tài khoản hiện tại: xóa token lưu trong DB và dọn dẹp SecurityContext
     */
    void logout();

    /**
     * Làm mới token (Refresh Token): kiểm tra refresh token hợp lệ và cấp cặp token mới
     *
     * @param refreshToken Chuỗi refresh token lấy từ Cookie của Client
     * @return DTO chứa thông tin tài khoản và cặp token mới
     */
    LoginResponseDto refreshToken(String refreshToken);
}
