package com.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Thực thể chi tiết mượn sách thuộc phiếu mượn (BorrowItem).
 * Đại diện cho số lượng sách mượn từ tồn kho của chi nhánh.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "borrow_items")
public class BorrowItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_slip_id", referencedColumnName = "id", nullable = false)
    private BorrowSlip borrowSlip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", referencedColumnName = "id", nullable = false)
    private Inventory inventory;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Đơn giá thuê của một cuốn sách được chốt tại thời điểm lập phiếu mượn.
     */
    @Column(name = "rental_price", precision = 12, scale = 2)
    private BigDecimal rentalPrice;
}
