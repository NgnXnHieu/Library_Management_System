package com.library.requestform.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequestForm {

    private String fullName;

    @Email(message = "Email không đúng định dạng")
    private String email;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải từ 10 đến 11 chữ số")
    private String phone;

    private LocalDate dateOfBirth;

    private String imageUrl;

    private String address;

    private String status;

    /**
     * Vai trò mới của người dùng (ADMIN, BRANCHMANAGER, STAFF, CUSTOMER)
     */
    private String role;

    /**
     * ID chi nhánh mới trực thuộc của người dùng
     */
    private Long branchId;
}
