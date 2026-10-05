package com.library.repository;

import com.library.dto.borrow.BorrowSlipResponseDto;
import com.library.entity.BorrowSlip;
import com.library.requestform.borrow.BorrowSlipFilterRequestForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * Lấy danh sách phiếu mượn phân trang kết hợp bộ lọc đa tiêu chí bằng JPQL Constructor Expression.
     * Chiếu trực tiếp kết quả lên BorrowSlipResponseDto, loại bỏ việc nạp các thực thể BorrowSlip, User, Branch vào bộ nhớ đệm.
     *
     * @param filter   Bộ lọc tìm kiếm (thời gian, mã phiếu, thông tin độc giả, chi nhánh, trạng thái...)
     * @param pageable Thông tin phân trang và sắp xếp
     * @return Trang kết quả chứa danh sách BorrowSlipResponseDto
     */
    @Query(value = "SELECT new com.library.dto.borrow.BorrowSlipResponseDto("
            + "b.id, b.borrowCode, c.id, c.fullName, c.phone, "
            + "s.id, s.fullName, s.phone, "
            + "br.id, br.name, "
            + "b.borrowedAt, b.dueAt, b.returnedAt, "
            + "b.status, b.paymentStatus, b.totalQuantity, b.totalAmount, "
            + "b.createdAt, b.updatedAt) "
            + "FROM BorrowSlip b "
            + "LEFT JOIN b.customer c "
            + "LEFT JOIN c.account a "
            + "LEFT JOIN b.staff s "
            + "LEFT JOIN b.branch br "
            + "WHERE (:#{#filter.fromBorrowedAt} IS NULL OR b.borrowedAt >= :#{#filter.fromBorrowedAt}) "
            + "AND (:#{#filter.toBorrowedAt} IS NULL OR b.borrowedAt <= :#{#filter.toBorrowedAt}) "
            + "AND (:#{#filter.fromReturnedAt} IS NULL OR b.returnedAt >= :#{#filter.fromReturnedAt}) "
            + "AND (:#{#filter.toReturnedAt} IS NULL OR b.returnedAt <= :#{#filter.toReturnedAt}) "
            + "AND (:#{#filter.borrowCode} IS NULL OR TRIM(:#{#filter.borrowCode}) = '' OR LOWER(b.borrowCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.borrowCode}), '%'))) "
            + "AND (:#{#filter.customerSearch} IS NULL OR TRIM(:#{#filter.customerSearch}) = '' OR ("
            + "   LOWER(c.phone) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.customerSearch}), '%')) "
            + "   OR LOWER(c.email) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.customerSearch}), '%')) "
            + "   OR LOWER(a.username) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.customerSearch}), '%')) "
            + "   OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.customerSearch}), '%')))) "
            + "AND (:#{#filter.customerId} IS NULL OR c.id = :#{#filter.customerId}) "
            + "AND (:#{#filter.branchId} IS NULL OR br.id = :#{#filter.branchId}) "
            + "AND (:#{#filter.status} IS NULL OR b.status = :#{#filter.status}) "
            + "AND (:#{#filter.paymentStatus} IS NULL OR b.paymentStatus = :#{#filter.paymentStatus})",
            countQuery = "SELECT count(b) FROM BorrowSlip b "
            + "LEFT JOIN b.customer c "
            + "LEFT JOIN c.account a "
            + "LEFT JOIN b.branch br "
            + "WHERE (:#{#filter.fromBorrowedAt} IS NULL OR b.borrowedAt >= :#{#filter.fromBorrowedAt}) "
            + "AND (:#{#filter.toBorrowedAt} IS NULL OR b.borrowedAt <= :#{#filter.toBorrowedAt}) "
            + "AND (:#{#filter.fromReturnedAt} IS NULL OR b.returnedAt >= :#{#filter.fromReturnedAt}) "
            + "AND (:#{#filter.toReturnedAt} IS NULL OR b.returnedAt <= :#{#filter.toReturnedAt}) "
            + "AND (:#{#filter.borrowCode} IS NULL OR TRIM(:#{#filter.borrowCode}) = '' OR LOWER(b.borrowCode) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.borrowCode}), '%'))) "
            + "AND (:#{#filter.customerSearch} IS NULL OR TRIM(:#{#filter.customerSearch}) = '' OR ("
            + "   LOWER(c.phone) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.customerSearch}), '%')) "
            + "   OR LOWER(c.email) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.customerSearch}), '%')) "
            + "   OR LOWER(a.username) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.customerSearch}), '%')) "
            + "   OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.customerSearch}), '%')))) "
            + "AND (:#{#filter.customerId} IS NULL OR c.id = :#{#filter.customerId}) "
            + "AND (:#{#filter.branchId} IS NULL OR br.id = :#{#filter.branchId}) "
            + "AND (:#{#filter.status} IS NULL OR b.status = :#{#filter.status}) "
            + "AND (:#{#filter.paymentStatus} IS NULL OR b.paymentStatus = :#{#filter.paymentStatus})")
    Page<BorrowSlipResponseDto> findAllBorrowSlipsWithFilter(@Param("filter") BorrowSlipFilterRequestForm filter, Pageable pageable);

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
