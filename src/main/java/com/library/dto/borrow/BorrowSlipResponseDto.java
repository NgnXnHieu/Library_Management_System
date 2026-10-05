package com.library.dto.borrow;

import com.library.enums.BorrowStatus;
import com.library.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO trả về thông tin đầy đủ của một phiếu mượn sách.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowSlipResponseDto {

    private Long id;
    private String borrowCode;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Long staffId;
    private String staffName;
    private String staffPhone;
    private Long branchId;
    private String branchName;
    private LocalDateTime borrowedAt;
    private LocalDateTime dueAt;
    private LocalDateTime returnedAt;
    private BorrowStatus status;
    private PaymentStatus paymentStatus;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private List<BorrowItemResponseDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor phục vụ JPQL Constructor Expression (SELECT new com.library.dto.borrow.BorrowSlipResponseDto(...)).
     * Nạp trực tiếp thông tin tóm tắt phiếu mượn, tránh nạp thực thể BorrowSlip, User, Branch vào bộ nhớ đệm Hibernate.
     */
    public BorrowSlipResponseDto(
            Long id,
            String borrowCode,
            Long customerId,
            String customerName,
            String customerPhone,
            Long staffId,
            String staffName,
            String staffPhone,
            Long branchId,
            String branchName,
            LocalDateTime borrowedAt,
            LocalDateTime dueAt,
            LocalDateTime returnedAt,
            BorrowStatus status,
            PaymentStatus paymentStatus,
            Integer totalQuantity,
            BigDecimal totalAmount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.borrowCode = borrowCode;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.staffId = staffId;
        this.staffName = staffName;
        this.staffPhone = staffPhone;
        this.branchId = branchId;
        this.branchName = branchName;
        this.borrowedAt = borrowedAt;
        this.dueAt = dueAt;
        this.returnedAt = returnedAt;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.totalQuantity = totalQuantity;
        this.totalAmount = totalAmount;
        this.items = new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
