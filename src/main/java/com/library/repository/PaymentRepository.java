package com.library.repository;

import com.library.entity.Payment;
import com.library.enums.PaymentPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác với bảng payments trong cơ sở dữ liệu.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Tìm thông tin thanh toán theo mã giao dịch duy nhất.
     *
     * @param paymentCode Mã thanh toán
     * @return Optional chứa Payment nếu tìm thấy
     */
    Optional<Payment> findByPaymentCode(String paymentCode);

    /**
     * Kiểm tra mã giao dịch thanh toán đã tồn tại chưa.
     *
     * @param paymentCode Mã thanh toán
     * @return true nếu đã tồn tại, ngược lại false
     */
    boolean existsByPaymentCode(String paymentCode);

    /**
     * Tìm danh sách các giao dịch thanh toán của phiếu mượn theo mục đích thanh toán (ví dụ: RENTAL_FEE).
     *
     * @param borrowSlipId   ID phiếu mượn
     * @param paymentPurpose Mục đích thanh toán
     * @return Danh sách các bản ghi Payment khớp điều kiện
     */
    List<Payment> findByBorrowSlipIdAndPaymentPurpose(Long borrowSlipId, PaymentPurpose paymentPurpose);
}
