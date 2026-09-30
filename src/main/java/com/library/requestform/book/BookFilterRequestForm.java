package com.library.requestform.book;

import com.library.enums.DisplayStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Form tiếp nhận các tham số lọc, tìm kiếm, sắp xếp và phân trang danh sách sách.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookFilterRequestForm {

    /**
     * Lọc theo trạng thái hiển thị (HIDE / UNHIDE)
     */
    private DisplayStatus status;

    /**
     * Lọc theo mã ISBN (tìm kiếm gần đúng / chứa)
     */
    private String isbn;

    /**
     * Lọc theo tiêu đề sách (tìm kiếm gần đúng / không phân biệt hoa thường)
     */
    private String title;

    /**
     * Khoảng năm xuất bản: từ năm
     */
    private Integer publicationYearFrom;

    /**
     * Khoảng năm xuất bản: đến năm
     */
    private Integer publicationYearTo;

    /**
     * Khoảng giá sách (giá gốc): từ giá
     */
    private BigDecimal minPrice;

    /**
     * Khoảng giá sách (giá gốc): đến giá
     */
    private BigDecimal maxPrice;

    /**
     * Khoảng giá mượn: từ giá mượn
     */
    private BigDecimal minRentalPrice;

    /**
     * Khoảng giá mượn: đến giá mượn
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
     * Trường sắp xếp (mặc định theo "id", hỗ trợ "price", "rentalPrice", "fineAmount", "publicationYear", "title", "createdAt")
     */
    @Builder.Default
    private String sortBy = "id";

    /**
     * Hướng sắp xếp: "asc" (tăng dần) hoặc "desc" (giảm dần)
     */
    @Builder.Default
    private String sortDir = "desc";

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
