package com.library.repository;

import com.library.entity.BorrowSlip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository thao tác với bảng borrow_slips trong Database.
 */
@Repository
public interface BorrowSlipRepository extends JpaRepository<BorrowSlip, Long>, JpaSpecificationExecutor<BorrowSlip> {

    /**
     * Kiểm tra mã phiếu mượn đã tồn tại trên hệ thống hay chưa.
     *
     * @param borrowCode Mã phiếu mượn
     * @return true nếu đã tồn tại, ngược lại false
     */
    boolean existsByBorrowCode(String borrowCode);

    /**
     * Tìm phiếu mượn theo mã phiếu.
     *
     * @param borrowCode Mã phiếu mượn
     * @return Optional chứa BorrowSlip nếu tìm thấy
     */
    Optional<BorrowSlip> findByBorrowCode(String borrowCode);

    /**
     * Tìm phiếu mượn theo ID kèm thông tin chi tiết customer, staff, branch và danh sách items.
     *
     * @param id ID phiếu mượn
     * @return Optional chứa BorrowSlip kèm chi tiết
     */
    @Query("SELECT bs FROM BorrowSlip bs " +
           "JOIN FETCH bs.customer c " +
           "JOIN FETCH bs.staff s " +
           "JOIN FETCH bs.branch b " +
           "LEFT JOIN FETCH bs.borrowItems bi " +
           "LEFT JOIN FETCH bi.inventory inv " +
           "LEFT JOIN FETCH inv.book bk " +
           "WHERE bs.id = :id")
    Optional<BorrowSlip> findByIdWithDetails(@Param("id") Long id);
}
