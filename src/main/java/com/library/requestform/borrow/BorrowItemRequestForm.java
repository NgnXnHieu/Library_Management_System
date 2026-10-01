package com.library.requestform.borrow;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request form thông tin một cuốn sách trong phiếu mượn (BorrowItem).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowItemRequestForm {

    /**
     * ID của bản ghi tồn kho sách tại chi nhánh.
     */
    @NotNull(message = "ID tồn kho sách (inventoryId) không được để trống")
    private Long inventoryId;

    /**
     * Số lượng sách mượn.
     */
    @NotNull(message = "Số lượng sách mượn không được để trống")
    @Min(value = 1, message = "Số lượng sách mượn tối thiểu là 1")
    private Integer quantity;
}
