package com.library.enums;

/**
 * Enum định nghĩa các trạng thái của phiếu mượn sách (BorrowSlip).
 */
public enum BorrowStatus {
    /**
     * Đang mượn (sách đang được độc giả mượn, chưa hoàn trả).
     */
    BORROWED,

    /**
     * Đã trả (độc giả đã hoàn trả đầy đủ sách).
     */
    RETURNED,

    /**
     * Quá hạn (đã vượt quá thời hạn hẹn trả nhưng chưa hoàn trả sách).
     */
    OVERDUE,

    /**
     * Đã hủy (phiếu mượn đã bị hủy, sách đã được hoàn trả lại vào kho).
     */
    CANCELLED
}
