package com.library.requestform.inventory;

import com.library.enums.DisplayStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Form tiếp nhận dữ liệu cập nhật thông tin tồn kho sách (vị trí kệ, trạng thái hiển thị).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUpdateRequestForm {

    /**
     * Vị trí lưu trữ kệ sách tại chi nhánh (ví dụ: "Kệ A-01, Tầng 2")
     */
    @Size(max = 100, message = "Vị trí kệ sách không được vượt quá 100 ký tự")
    private String shelfLocation;

    /**
     * Trạng thái hiển thị trong kho (HIDE / UNHIDE)
     */
    private DisplayStatus status;
}
