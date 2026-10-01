package com.library.enums;

/**
 * Enum định nghĩa các phương thức thanh toán trong hệ thống.
 */
public enum PaymentMethod {
    /**
     * Thanh toán bằng tiền mặt trực tiếp tại quầy thư viện.
     */
    CASH,

    /**
     * Thanh toán bằng hình thức chuyển khoản ngân hàng (qua mã QR, số tài khoản...).
     */
    BANK_TRANSFER
}
