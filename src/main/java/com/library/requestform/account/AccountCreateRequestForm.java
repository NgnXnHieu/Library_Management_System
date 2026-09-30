package com.library.requestform.account;

import com.library.enums.AccountStatus;
import com.library.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request form dành cho Admin tạo tài khoản người dùng kèm vai trò và chi nhánh.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCreateRequestForm {

    @NotBlank(message = "Tài khoản không được để trống")
    @Size(min = 10, max = 50, message = "Tài khoản phải từ 10 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`]).{8,}$",
        message = "Mật khẩu phải từ 8 ký tự trở lên, chứa ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt"
    )
    private String password;

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotNull(message = "Vai trò không được để trống")
    private Role role;

    @NotNull(message = "Trạng thái không được để trống")
    private AccountStatus status;

    @NotNull(message = "Mã chi nhánh không được để trống")
    private Long branchId;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải từ 10 đến 11 chữ số")
    private String phone;
}
