package com.library.requestform.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Form tiếp nhận dữ liệu nhập thêm sách vào kho chi nhánh.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryImportRequestForm {

    /**
     * Số lượng sách nhập thêm vào kho (bắt buộc, tối thiểu 1 cuốn)
     */
    @NotNull(message = "Số lượng nhập kho không được để trống")
    @Min(value = 1, message = "Số lượng nhập kho phải lớn hơn hoặc bằng 1")
    private Integer quantity;
}
