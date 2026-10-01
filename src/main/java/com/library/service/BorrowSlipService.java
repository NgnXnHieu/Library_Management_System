package com.library.service;

import com.library.dto.borrow.BorrowSlipResponseDto;
import com.library.requestform.borrow.BorrowSlipCreateRequestForm;
import com.library.requestform.borrow.BorrowSlipFilterRequestForm;
import org.springframework.data.domain.Page;

/**
 * Interface định nghĩa các phương thức xử lý nghiệp vụ quản lý phiếu mượn sách.
 */
public interface BorrowSlipService {

    /**
     * Tạo mới phiếu mượn sách với chi nhánh và nhân viên chỉ định (dùng chung cho cả Admin và nội bộ chi nhánh).
     *
     * @param form     Dữ liệu tạo phiếu mượn gồm customerId và danh sách tồn kho sách kèm số lượng
     * @param branchId ID chi nhánh thực hiện mượn sách
     * @param staffId  ID nhân viên tạo phiếu mượn
     * @return DTO thông tin phiếu mượn vừa được tạo
     */
    BorrowSlipResponseDto createBorrowSlip(BorrowSlipCreateRequestForm form, Long branchId, Long staffId);

    /**
     * Tạo mới phiếu mượn sách tại chi nhánh làm việc của nhân viên đang đăng nhập.
     * Tự động trích xuất branchId và staffId từ SecurityContextHolder.
     *
     * @param form Dữ liệu tạo phiếu mượn gồm customerId và danh sách tồn kho sách kèm số lượng
     * @return DTO thông tin phiếu mượn vừa được tạo
     */
    BorrowSlipResponseDto createBorrowSlipForCurrentStaffBranch(BorrowSlipCreateRequestForm form);

    /**
     * Lấy danh sách phiếu mượn phân trang kèm bộ lọc và sắp xếp (Dành cho Admin, cho phép lọc theo branchId).
     *
     * @param filter Bộ lọc tìm kiếm, sắp xếp và phân trang
     * @return Trang kết quả chứa danh sách phiếu mượn
     */
    Page<BorrowSlipResponseDto> getAllBorrowSlips(BorrowSlipFilterRequestForm filter);

    /**
     * Lấy danh sách phiếu mượn phân trang theo chi nhánh của nhân viên/quản lý đang đăng nhập.
     * branchId được tự động trích xuất từ tài khoản hiện tại và gán vào bộ lọc.
     *
     * @param filter Bộ lọc tìm kiếm, sắp xếp và phân trang
     * @return Trang kết quả chứa danh sách phiếu mượn của chi nhánh
     */
    Page<BorrowSlipResponseDto> getBorrowSlipsByCurrentBranch(BorrowSlipFilterRequestForm filter);

    /**
     * Hủy phiếu mượn sách, hoàn trả số lượng tồn kho sách về chi nhánh và cập nhật trạng thái thanh toán.
     *
     * @param id ID của phiếu mượn cần hủy
     * @return DTO thông tin phiếu mượn sau khi hủy
     */
    BorrowSlipResponseDto cancelBorrowSlip(Long id);
}
