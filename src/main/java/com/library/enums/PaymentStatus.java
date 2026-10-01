package com.library.enums;

/**
 * Enum định nghĩa các trạng thái thanh toán của giao dịch.
 */
public enum PaymentStatus {
    /**
     * Chưa thanh toán (khoản tiền chưa được trả).
     */
    UNPAID,

    /**
     * Đã thanh toán thành công.
     */
    PAID,

    /**
     * Đã hoàn tiền (khoản tiền đã được hoàn trả lại cho khách).
     */
    REFUNDED,

    /**
     * Đã hủy (giao dịch thanh toán đã bị hủy do hủy phiếu mượn).
     */
    CANCELLED
}
