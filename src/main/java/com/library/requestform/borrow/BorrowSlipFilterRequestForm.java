package com.library.requestform.borrow;

import com.library.enums.BorrowStatus;
import com.library.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Form tiếp nhận các tham số lọc, tìm kiếm, sắp xếp và phân trang danh sách phiếu mượn (BorrowSlip).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowSlipFilterRequestForm {

    /**
     * Lọc theo thời gian mượn: từ thời điểm (ISO-8601, ví dụ: 2026-10-01T00:00:00).
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromBorrowedAt;

    /**
     * Lọc theo thời gian mượn: đến thời điểm (ISO-8601, ví dụ: 2026-10-01T23:59:59).
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toBorrowedAt;

    /**
     * Lọc theo thời gian trả: từ thời điểm.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromReturnedAt;

    /**
     * Lọc theo thời gian trả: đến thời điểm.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toReturnedAt;

    /**
     * Lọc theo mã phiếu mượn (tìm kiếm gần đúng/chứa chuỗi).
     */
    private String borrowCode;

    /**
     * Tìm kiếm thông tin khách hàng chung (tìm kiếm trong số điện thoại, username, email, họ tên).
     */
    private String customerSearch;

    /**
     * Lọc chính xác theo ID khách hàng (customerId).
     */
    private Long customerId;

    /**
     * Lọc theo ID chi nhánh thư viện.
     */
    private Long branchId;

    /**
     * Lọc theo trạng thái phiếu mượn (BORROWED, RETURNED, OVERDUE).
     */
    private BorrowStatus status;

    /**
     * Lọc theo trạng thái thanh toán của phiếu (UNPAID, PAID).
     */
    private PaymentStatus paymentStatus;

    /**
     * Trường sắp xếp: hỗ trợ "borrowedAt", "returnedAt", "totalAmount". Mặc định: "borrowedAt".
     */
    @Builder.Default
    private String sortBy = "borrowedAt";

    /**
     * Hướng sắp xếp: "asc" (tăng dần / từ cũ đến mới) hoặc "desc" (giảm dần / mới đến cũ). Mặc định: "asc".
     */
    @Builder.Default
    private String sortDir = "asc";

    /**
     * Chỉ số trang cần lấy (bắt đầu từ 0). Mặc định: 0.
     */
    @Builder.Default
    private int page = 0;

    /**
     * Số lượng phần tử trên mỗi trang. Mặc định: 10.
     */
    @Builder.Default
    private int size = 10;
}
