package com.library.requestform.branch;

import com.library.enums.BranchStatus;
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
 * Request form tạo mới chi nhánh thư viện.
 * Bắt buộc nhập: code, name, address, status (BranchStatus: OPEN, CLOSED).
 * Không bắt buộc: phone.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchCreateRequestForm {

    @NotBlank(message = "Mã chi nhánh không được để trống")
    @Size(max = 20, message = "Mã chi nhánh tối đa 20 ký tự")
    private String code;

    @NotBlank(message = "Tên chi nhánh không được để trống")
    @Size(max = 100, message = "Tên chi nhánh tối đa 100 ký tự")
    private String name;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String address;

    @NotNull(message = "Trạng thái không được để trống")
    private BranchStatus status;

    @Pattern(regexp = "^$|^[0-9]{10,11}$", message = "Số điện thoại phải từ 10 đến 11 chữ số")
    private String phone;

    @Size(max = 255, message = "Đường dẫn ảnh tối đa 255 ký tự")
    private String imageUrl;
}
