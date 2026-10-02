package com.library.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO chứa kết quả trả về khi người dùng đăng nhập thành công.
 * - JSON body chỉ chứa: username, fullName, role để phục vụ hiển thị và phân quyền Frontend.
 * - accessToken và refreshToken được đánh dấu @JsonIgnore để không lộ ra body (được lưu trong Cookies).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {

    // Thông tin tài khoản trả về JSON cho Frontend
    private String username;
    private String fullName;
    private String role;

    // Token dùng nội bộ để Controller tạo HttpOnly Cookie, ẩn khỏi JSON Body
    @JsonIgnore
    private String accessToken;

    @JsonIgnore
    private String refreshToken;
}
