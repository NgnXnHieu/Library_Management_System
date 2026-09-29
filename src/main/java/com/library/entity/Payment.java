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
import java.time.LocalDateTime;

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

    @Column(name = "payment_type", length = 30)
    private String paymentType;

    @Column(name = "provider", length = 30)
    private String provider;

    @Column(name = "transaction_code", length = 100)
    private String transactionCode;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
