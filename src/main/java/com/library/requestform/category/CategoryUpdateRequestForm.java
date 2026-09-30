package com.library.requestform.category;

import com.library.enums.DisplayStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request form cập nhật thông tin thể loại sách.
 * Các trường để trống/null sẽ được giữ nguyên giá trị cũ.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryUpdateRequestForm {

    @Size(max = 100, message = "Tên thể loại tối đa 100 ký tự")
    private String name;

    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;

    private DisplayStatus status;
}
