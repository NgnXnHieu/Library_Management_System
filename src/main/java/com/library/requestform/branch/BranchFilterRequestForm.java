package com.library.requestform.branch;

import com.library.enums.BranchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Form tiếp nhận các tham số tìm kiếm, lọc và phân trang danh sách chi nhánh (Branch).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchFilterRequestForm {

    /**
     * Lọc theo mã chi nhánh (tìm kiếm gần đúng / không phân biệt hoa thường)
     */
    private String code;

    /**
     * Lọc theo tên chi nhánh (tìm kiếm gần đúng / không phân biệt hoa thường)
     */
    private String name;

    /**
     * Lọc theo địa chỉ chi nhánh (tìm kiếm gần đúng / không phân biệt hoa thường)
     */
    private String address;

    /**
     * Lọc theo số điện thoại chi nhánh (tìm kiếm gần đúng)
     */
    private String phone;

    /**
     * Lọc theo trạng thái hoạt động của chi nhánh (OPEN, CLOSED)
     */
    private BranchStatus status;

    /**
     * Lọc theo thời gian mượn: từ thời điểm (ISO-8601, ví dụ: 2026-10-05T00:00:00).
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate;

    /**
     * Lọc theo thời gian mượn: đến thời điểm (ISO-8601, ví dụ: 2026-10-05T23:59:59).
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;

    /**
     * Số trang hiện tại (bắt đầu từ 0)
     */
    @Builder.Default
    private int page = 0;

    /**
     * Số lượng phần tử mỗi trang (mặc định 10)
     */
    @Builder.Default
    private int size = 10;

    /**
     * Trường sắp xếp dữ liệu (mặc định "createdAt" theo yêu cầu sắp xếp thời gian tạo mới nhất lên đầu)
     */
    @Builder.Default
    private String sortBy = "createdAt";

    /**
     * Hướng sắp xếp: "desc" (mới nhất lên đầu) hoặc "asc" (cũ nhất lên đầu)
     */
    @Builder.Default
    private String sortDir = "desc";
}
