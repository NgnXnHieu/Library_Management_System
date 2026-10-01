package com.library.enums;

/**
 * Enum định nghĩa mục đích hoặc loại khoản thu thanh toán trong hệ thống.
 */
public enum PaymentPurpose {
    /**
     * Tiền thuê sách cho phiếu mượn.
     */
    RENTAL_FEE,

    /**
     * Tiền phạt quá hạn do trả sách trễ so với thời hạn quy định.
     */
    OVERDUE_FINE,

    /**
     * Tiền bồi thường do làm hỏng hoặc mất sách.
     */
    COMPENSATION
}
