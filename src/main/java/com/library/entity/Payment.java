package com.library.entity;

import com.library.enums.PaymentMethod;
import com.library.enums.PaymentPurpose;
import com.library.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thực thể quản lý các giao dịch thanh toán trong hệ thống thư viện.
 * Bao gồm thanh toán tiền thuê sách, tiền phạt quá hạn và bồi thường hư hỏng
 * sách.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Column(name = "payment_code", length = 30, nullable = false, unique = true)
    private String paymentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_slip_id", referencedColumnName = "id")
    private BorrowSlip borrowSlip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    private User customer;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    /**
     * Phương thức thanh toán (CASH: Tiền mặt, BANK_TRANSFER: Chuyển khoản).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    /**
     * Mục đích thanh toán (RENTAL_FEE: Tiền thuê sách, OVERDUE_FINE: Tiền phạt quá
     * hạn, COMPENSATION: Bồi thường).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_purpose", length = 30)
    private PaymentPurpose paymentPurpose;

    @Column(name = "provider", length = 30)
    private String provider;

    @Column(name = "transaction_code", length = 100)
    private String transactionCode;

    /**
     * Trạng thái thanh toán (UNPAID: Chưa trả, PAID: Đã trả).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
