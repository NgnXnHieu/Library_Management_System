package com.library.dto.branch;

import com.library.enums.BranchStatus;
import com.library.util.FileUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO chứa thông tin thống kê tổng hợp của chi nhánh thư viện.
 * Phục vụ cho API phân trang thống kê chi nhánh của ADMIN sử dụng JPQL Constructor Expression.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchStatisticResponseDto {

    private Long id;
    private String code;
    private String name;
    private BranchStatus status;
    private String imageUrl;
    private Long totalBooksInStock;
    private Long totalAvailableBooks;
    private Long totalBorrowedBooks;
    private Long totalBorrowSlips;
    private BigDecimal totalRevenue;

    /**
     * Constructor phục vụ JPQL Constructor Expression (SELECT new com.library.dto.branch.BranchStatisticResponseDto(...)).
     * Tự động chuẩn hóa đường dẫn ảnh, xử lý giá trị null và tính toán số sách đang mượn.
     *
     * @param id                  ID chi nhánh
     * @param code                Mã định danh chi nhánh
     * @param name                Tên chi nhánh
     * @param status              Trạng thái hoạt động của chi nhánh (OPEN, CLOSED)
     * @param imageUrl            Khóa/đường dẫn ảnh của chi nhánh
     * @param totalBooksInStock   Tổng số lượng sách trong kho (SUM totalQuantity của inventories)
     * @param totalAvailableBooks Tổng số lượng sách còn khả dụng (SUM availableQuantity của inventories)
     * @param totalBorrowSlips    Tổng số lượt mượn sách hợp lệ (BORROWED, RETURNED, OVERDUE)
     * @param totalRevenue        Tổng doanh thu tiền thuê sách (SUM totalAmount của phiếu mượn hợp lệ)
     */
    public BranchStatisticResponseDto(
            Long id,
            String code,
            String name,
            BranchStatus status,
            String imageUrl,
            Long totalBooksInStock,
            Long totalAvailableBooks,
            Long totalBorrowSlips,
            BigDecimal totalRevenue
    ) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.status = status;
        this.imageUrl = FileUtil.buildFileUrl(imageUrl);
        this.totalBooksInStock = totalBooksInStock != null ? totalBooksInStock : 0L;
        this.totalAvailableBooks = totalAvailableBooks != null ? totalAvailableBooks : 0L;
        this.totalBorrowedBooks = Math.max(0L, this.totalBooksInStock - this.totalAvailableBooks);
        this.totalBorrowSlips = totalBorrowSlips != null ? totalBorrowSlips : 0L;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }
}
