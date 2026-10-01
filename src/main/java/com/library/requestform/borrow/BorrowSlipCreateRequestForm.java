package com.library.requestform.borrow;

import com.library.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request form tạo mới phiếu mượn sách (BorrowSlip).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowSlipCreateRequestForm {

    /**
     * ID người dùng của độc giả mượn sách.
     */
    @NotNull(message = "ID khách hàng (customerId) không được để trống")
    private Long customerId;

    /**
     * Danh sách các cuốn sách mượn kèm số lượng từ tồn kho của chi nhánh.
     */
    @NotEmpty(message = "Danh sách sách mượn không được để trống")
    @Valid
    private List<BorrowItemRequestForm> items;

    /**
     * Phương thức thanh toán (CASH: Tiền mặt, BANK_TRANSFER: Chuyển khoản).
     * Tùy chọn: Để trống nếu chưa thanh toán ngay khi tạo phiếu.
     */
    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;
}
