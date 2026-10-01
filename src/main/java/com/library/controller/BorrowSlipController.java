package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.borrow.BorrowSlipResponseDto;
import com.library.requestform.borrow.BorrowSlipCreateRequestForm;
import com.library.requestform.borrow.BorrowSlipFilterRequestForm;
import com.library.service.BorrowSlipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller tiếp nhận và xử lý các yêu cầu liên quan đến quản lý phiếu mượn
 * sách (BorrowSlip).
 */
@RestController
@RequiredArgsConstructor
public class BorrowSlipController {

    private final BorrowSlipService borrowSlipService;

    /**
     * API tạo phiếu mượn sách tại chi nhánh làm việc của nhân viên đang đăng nhập.
     * Chi nhánh và nhân viên lập phiếu được trích xuất tự động từ
     * SecurityContextHolder.
     * Dành riêng cho nhân viên và quản lý chi nhánh (STAFF, BRANCHMANAGER).
     *
     * @param form Thông tin tạo phiếu mượn gồm customerId và danh sách sách mượn
     * @return DTO thông tin phiếu mượn vừa được tạo bọc trong chuẩn ApiResponse với
     *         mã HTTP 201 CREATED
     */
    @PostMapping("/borrow-slips")
    @PreAuthorize("hasAnyRole('BRANCHMANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<BorrowSlipResponseDto>> createBorrowSlip(
            @Valid @RequestBody BorrowSlipCreateRequestForm form) {
        // Bước 1: Gọi tầng Service xử lý nghiệp vụ tạo phiếu mượn cho chi nhánh hiện
        // tại
        BorrowSlipResponseDto result = borrowSlipService.createBorrowSlipForCurrentStaffBranch(form);

        // Bước 2: Bọc kết quả trả về trong chuẩn ApiResponse với mã HTTP 201 CREATED
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo phiếu mượn sách thành công!", result));
    }

    /**
     * API lấy danh sách toàn bộ phiếu mượn phân trang kèm bộ lọc và sắp xếp.
     * Cho phép truyền branchId bất kỳ trong bộ lọc. Dành riêng cho ADMIN.
     *
     * @param filter Bộ lọc tìm kiếm, sắp xếp và phân trang nhận qua Query
     *               Parameters
     * @return Trang kết quả chứa danh sách phiếu mượn bọc trong chuẩn ApiResponse
     */
    @GetMapping("/admin/borrow-slips")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<BorrowSlipResponseDto>>> getAllBorrowSlips(
            @ModelAttribute BorrowSlipFilterRequestForm filter) {
        // Bước 1: Gọi tầng Service xử lý truy vấn phân trang toàn hệ thống
        Page<BorrowSlipResponseDto> result = borrowSlipService.getAllBorrowSlips(filter);

        // Bước 2: Bọc kết quả trả về trong chuẩn ApiResponse với HTTP 200 OK
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phiếu mượn thành công!", result));
    }

    /**
     * API lấy danh sách phiếu mượn phân trang tại chi nhánh của Quản lý chi nhánh
     * đang đăng nhập.
     * branchId được tự động trích xuất từ tài khoản hiện tại và áp đặt vào bộ lọc.
     * Dành riêng cho Quản lý chi nhánh (BRANCHMANAGER).
     *
     * @param filter Bộ lọc tìm kiếm, sắp xếp và phân trang nhận qua Query
     *               Parameters
     * @return Trang kết quả chứa danh sách phiếu mượn của chi nhánh bọc trong chuẩn
     *         ApiResponse
     */
    @GetMapping("/borrow-slips/current-branch")
    @PreAuthorize("hasAnyRole('BRANCHMANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<BorrowSlipResponseDto>>> getMyBranchBorrowSlips(
            @ModelAttribute BorrowSlipFilterRequestForm filter) {
        // Bước 1: Gọi tầng Service xử lý truy vấn theo chi nhánh hiện tại
        Page<BorrowSlipResponseDto> result = borrowSlipService.getBorrowSlipsByCurrentBranch(filter);

        // Bước 2: Bọc kết quả trả về trong chuẩn ApiResponse với HTTP 200 OK
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phiếu mượn của chi nhánh thành công!", result));
    }

    /**
     * API hủy phiếu mượn sách, hoàn trả số lượng sách về tồn kho và cập nhật trạng thái thanh toán.
     * Chỉ nhân viên hoặc quản lý tại chi nhánh của phiếu mượn mới có quyền thực hiện.
     * Dành riêng cho STAFF và BRANCHMANAGER.
     *
     * @param id ID của phiếu mượn cần hủy
     * @return DTO thông tin phiếu mượn sau khi hủy bọc trong chuẩn ApiResponse với HTTP 200 OK
     */
    @PutMapping("/borrow-slips/{id}/cancel")
    @PreAuthorize("hasAnyRole('BRANCHMANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<BorrowSlipResponseDto>> cancelBorrowSlip(@PathVariable Long id) {
        // Bước 1: Gọi tầng Service xử lý nghiệp vụ hủy phiếu, hoàn tồn kho và cập nhật thanh toán
        BorrowSlipResponseDto result = borrowSlipService.cancelBorrowSlip(id);

        // Bước 2: Bọc kết quả trả về trong chuẩn ApiResponse với HTTP 200 OK
        return ResponseEntity.ok(ApiResponse.success("Hủy phiếu mượn sách thành công!", result));
    }
}
