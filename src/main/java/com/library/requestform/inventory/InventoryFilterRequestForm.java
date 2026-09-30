package com.library.requestform.inventory;

import com.library.enums.DisplayStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Form tiếp nhận các tham số lọc, tìm kiếm, sắp xếp và phân trang danh sách tồn kho sách.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryFilterRequestForm {

    /**
     * Lọc theo ID chi nhánh
     */
    private Long branchId;

    /**
     * Lọc theo ID thể loại sách
     */
    private Long categoryId;

    /**
     * Lọc theo tiêu đề sách (tìm kiếm gần đúng / không phân biệt hoa thường)
     */
    private String bookTitle;

    /**
     * Khoảng giá sách (giá bìa): từ giá
     */
    private BigDecimal minPrice;

    /**
     * Khoảng giá sách (giá bìa): đến giá
     */
    private BigDecimal maxPrice;

    /**
     * Khoảng giá thuê/mượn sách: từ giá
     */
    private BigDecimal minRentalPrice;

    /**
     * Khoảng giá thuê/mượn sách: đến giá
     */
    private BigDecimal maxRentalPrice;

    /**
     * Khoảng tiền phạt: từ tiền phạt
     */
    private BigDecimal minFineAmount;

    /**
     * Khoảng tiền phạt: đến tiền phạt
     */
    private BigDecimal maxFineAmount;

    /**
     * Lọc theo trạng thái hiển thị của kho (HIDE / UNHIDE)
     */
    private DisplayStatus status;

    /**
     * Lọc theo vị trí kệ sách (tìm kiếm gần đúng)
     */
    private String shelfLocation;

    /**
     * Trường sắp xếp (mặc định "updatedAt", hỗ trợ "bookTitle", "totalQuantity", "availableQuantity", "price", "rentalPrice", "fineAmount", "createdAt", "updatedAt")
     */
    @Builder.Default
    private String sortBy = "updatedAt";

    /**
     * Hướng sắp xếp: "asc" (tăng dần / cũ -> mới) hoặc "desc" (giảm dần / mới -> cũ)
     */
    @Builder.Default
    private String sortDir = "asc";

    /**
     * Số trang (bắt đầu từ 0)
     */
    @Builder.Default
    private int page = 0;

    /**
     * Số lượng phần tử mỗi trang (mặc định 10)
     */
    @Builder.Default
    private int size = 10;
}
