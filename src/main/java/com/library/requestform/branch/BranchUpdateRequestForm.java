package com.library.requestform.branch;

import com.library.enums.BranchStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request form cập nhật thông tin chi nhánh thư viện.
 * Các trường để trống/null sẽ được giữ nguyên giá trị cũ.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchUpdateRequestForm {

    @Size(max = 20, message = "Mã chi nhánh tối đa 20 ký tự")
    private String code;

    @Size(max = 100, message = "Tên chi nhánh tối đa 100 ký tự")
    private String name;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String address;

    @Pattern(regexp = "^$|^[0-9]{10,11}$", message = "Số điện thoại phải từ 10 đến 11 chữ số")
    private String phone;

    private BranchStatus status;
 
    @Size(max = 255, message = "Đường dẫn ảnh tối đa 255 ký tự")
    private String imageUrl;
}
