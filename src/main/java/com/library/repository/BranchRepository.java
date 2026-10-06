package com.library.repository;

import com.library.dto.branch.BranchStatisticResponseDto;
import com.library.entity.Branch;
import com.library.enums.BorrowStatus;
import com.library.requestform.branch.BranchFilterRequestForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác với bảng branches trong Database.
 * Kế thừa JpaSpecificationExecutor để hỗ trợ lọc động qua Specification.
 */
@Repository
public interface BranchRepository extends JpaRepository<Branch, Long>, JpaSpecificationExecutor<Branch> {

    Optional<Branch> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * Lấy danh sách thống kê chi nhánh phân trang kết hợp bộ lọc bằng JPQL Constructor Expression.
     * Sử dụng scalar subqueries để tránh bẫy tích Descartes khi tổng hợp đồng thời tồn kho và phiếu mượn.
     *
     * @param filter         Bộ lọc tìm kiếm (code, name, status)
     * @param borrowStatuses Danh sách trạng thái phiếu mượn hợp lệ được tính thống kê
     * @param pageable       Thông tin phân trang và sắp xếp
     * @return Trang kết quả chứa danh sách BranchStatisticResponseDto
     */
    @Query(value = "SELECT new com.library.dto.branch.BranchStatisticResponseDto("
            + "b.id, b.code, b.name, b.status, b.imageUrl, "
            + "(SELECT SUM(i.totalQuantity) FROM Inventory i WHERE i.branch.id = b.id), "
            + "(SELECT SUM(i.availableQuantity) FROM Inventory i WHERE i.branch.id = b.id), "
            + "(SELECT COUNT(bs.id) FROM BorrowSlip bs WHERE bs.branch.id = b.id AND bs.status IN :borrowStatuses "
            + "   AND (:#{#filter.fromDate} IS NULL OR bs.borrowedAt >= :#{#filter.fromDate}) "
            + "   AND (:#{#filter.toDate} IS NULL OR bs.borrowedAt <= :#{#filter.toDate})), "
            + "(SELECT SUM(bs.totalAmount) FROM BorrowSlip bs WHERE bs.branch.id = b.id AND bs.status IN :borrowStatuses "
            + "   AND (:#{#filter.fromDate} IS NULL OR bs.borrowedAt >= :#{#filter.fromDate}) "
            + "   AND (:#{#filter.toDate} IS NULL OR bs.borrowedAt <= :#{#filter.toDate}))) "
            + "FROM Branch b "
            + "WHERE (:#{#filter.code} IS NULL OR TRIM(:#{#filter.code}) = '' OR LOWER(b.code) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.code}), '%'))) "
            + "AND (:#{#filter.name} IS NULL OR TRIM(:#{#filter.name}) = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.name}), '%'))) "
            + "AND (:#{#filter.status} IS NULL OR b.status = :#{#filter.status})",
            countQuery = "SELECT COUNT(b) FROM Branch b "
            + "WHERE (:#{#filter.code} IS NULL OR TRIM(:#{#filter.code}) = '' OR LOWER(b.code) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.code}), '%'))) "
            + "AND (:#{#filter.name} IS NULL OR TRIM(:#{#filter.name}) = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.name}), '%'))) "
            + "AND (:#{#filter.status} IS NULL OR b.status = :#{#filter.status})")
    Page<BranchStatisticResponseDto> findBranchStatisticsWithFilter(
            @Param("filter") BranchFilterRequestForm filter,
            @Param("borrowStatuses") List<BorrowStatus> borrowStatuses,
            Pageable pageable);

    /**
     * Lấy danh sách toàn bộ thống kê chi nhánh (không phân trang) để phục vụ xuất báo cáo Excel.
     * Sử dụng JPQL Constructor Expression chiếu trực tiếp dữ liệu lên BranchStatisticResponseDto.
     *
     * @param filter         Bộ lọc tìm kiếm (code, name, status, fromDate, toDate)
     * @param borrowStatuses Danh sách trạng thái phiếu mượn hợp lệ được tính thống kê
     * @return Danh sách toàn bộ BranchStatisticResponseDto thỏa mãn điều kiện
     */
    @Query(value = "SELECT new com.library.dto.branch.BranchStatisticResponseDto("
            + "b.id, b.code, b.name, b.status, b.imageUrl, "
            + "(SELECT SUM(i.totalQuantity) FROM Inventory i WHERE i.branch.id = b.id), "
            + "(SELECT SUM(i.availableQuantity) FROM Inventory i WHERE i.branch.id = b.id), "
            + "(SELECT COUNT(bs.id) FROM BorrowSlip bs WHERE bs.branch.id = b.id AND bs.status IN :borrowStatuses "
            + "   AND (:#{#filter.fromDate} IS NULL OR bs.borrowedAt >= :#{#filter.fromDate}) "
            + "   AND (:#{#filter.toDate} IS NULL OR bs.borrowedAt <= :#{#filter.toDate})), "
            + "(SELECT SUM(bs.totalAmount) FROM BorrowSlip bs WHERE bs.branch.id = b.id AND bs.status IN :borrowStatuses "
            + "   AND (:#{#filter.fromDate} IS NULL OR bs.borrowedAt >= :#{#filter.fromDate}) "
            + "   AND (:#{#filter.toDate} IS NULL OR bs.borrowedAt <= :#{#filter.toDate}))) "
            + "FROM Branch b "
            + "WHERE (:#{#filter.code} IS NULL OR TRIM(:#{#filter.code}) = '' OR LOWER(b.code) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.code}), '%'))) "
            + "AND (:#{#filter.name} IS NULL OR TRIM(:#{#filter.name}) = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.name}), '%'))) "
            + "AND (:#{#filter.status} IS NULL OR b.status = :#{#filter.status}) "
            + "ORDER BY b.createdAt DESC")
    List<BranchStatisticResponseDto> findAllBranchStatisticsWithFilter(
            @Param("filter") BranchFilterRequestForm filter,
            @Param("borrowStatuses") List<BorrowStatus> borrowStatuses);
}


