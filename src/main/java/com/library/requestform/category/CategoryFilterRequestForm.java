package com.library.requestform.category;

import com.library.enums.DisplayStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Form tiếp nhận các tham số tìm kiếm, lọc và phân trang danh sách thể loại sách (Category).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryFilterRequestForm {

    /**
     * Lọc theo tên thể loại (tìm kiếm gần đúng, không phân biệt hoa thường)
     */
    private String name;

    /**
     * Lọc theo mô tả thể loại (tìm kiếm gần đúng, không phân biệt hoa thường)
     */
    private String description;

    /**
     * Lọc theo trạng thái hiển thị (HIDE, UNHIDE)
     */
    private DisplayStatus status;

    /**
     * Chỉ số trang hiện tại (0-indexed, bắt đầu từ 0)
     */
    @Builder.Default
    private int page = 0;

    /**
     * Số lượng phần tử trên mỗi trang (mặc định 10)
     */
    @Builder.Default
    private int size = 10;

    /**
     * Trường dữ liệu dùng để sắp xếp (mặc định "createdAt" theo yêu cầu mới nhất lên đầu)
     */
    @Builder.Default
    private String sortBy = "createdAt";

    /**
     * Hướng sắp xếp: "desc" (giảm dần, mới nhất lên đầu) hoặc "asc" (tăng dần, cũ nhất lên đầu)
     */
    @Builder.Default
    private String sortDir = "desc";
}
