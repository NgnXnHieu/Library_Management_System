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
}
