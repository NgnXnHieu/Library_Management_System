package com.library.entity;

import com.library.enums.BorrowStatus;
import com.library.enums.PaymentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể phiếu mượn sách (BorrowSlip).
 * Quản lý thông tin độc giả mượn, nhân viên tạo phiếu, chi nhánh và trạng thái mượn trả.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "borrow_slips")
public class BorrowSlip extends BaseEntity {

    @Column(name = "borrow_code", length = 30, nullable = false, unique = true)
    private String borrowCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", referencedColumnName = "id", nullable = false)
    private User staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", referencedColumnName = "id", nullable = false)
    private Branch branch;

    @Column(name = "borrowed_at", nullable = false)
    private LocalDateTime borrowedAt;

    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    /**
     * Trạng thái của phiếu mượn (BORROWED: đang mượn, RETURNED: đã trả, OVERDUE: quá hạn).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private BorrowStatus status;

    /**
     * Trạng thái thanh toán của phiếu mượn (UNPAID: Chưa thanh toán, PAID: Đã thanh toán).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20, nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    /**
     * Tổng số lượng sách mượn trong phiếu.
     */
    @Column(name = "total_quantity")
    private Integer totalQuantity;

    /**
     * Tổng tiền thuê sách của phiếu mượn (tính từ tổng số lượng của từng cuốn * giá thuê rentalPrice).
     */
    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Builder.Default
    @OneToMany(mappedBy = "borrowSlip", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BorrowItem> borrowItems = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "borrowSlip", fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();
}
