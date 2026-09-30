package com.library.service;

import com.library.dto.inventory.InventoryResponseDto;
import com.library.requestform.inventory.InventoryFilterRequestForm;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interface định nghĩa các nghiệp vụ quản lý tồn kho sách (Inventory).
 */
public interface InventoryService {

    /**
     * Tự động khởi tạo tồn kho cho tất cả các cuốn sách đang có trong hệ thống tại
     * một chi nhánh.
     * Chỉ tạo mới cho các cuốn sách chưa có trong kho của chi nhánh đó.
     *
     * @param branchId ID chi nhánh cần khởi tạo tồn kho
     * @return Danh sách DTO các bản ghi tồn kho vừa được tạo mới
     */
    List<InventoryResponseDto> initInventoriesForBranch(Long branchId);

    /**
     * Lấy danh sách tồn kho sách phân trang kèm theo bộ lọc tìm kiếm và sắp xếp.
     * Sử dụng JOIN FETCH đa tầng để tối ưu hiệu năng và tránh N+1 query.
     *
     * @param filter Bộ lọc tìm kiếm và phân trang
     * @return Trang kết quả chứa danh sách InventoryResponseDto
     */
    Page<InventoryResponseDto> getAllInventories(InventoryFilterRequestForm filter);

    /**
     * Lấy danh sách phân trang tồn kho theo chi nhánh làm việc của nhân viên đang
     * đăng nhập.
     * Tự động lấy branchId từ SecurityContextHolder và gán vào bộ lọc.
     *
     * @param filter Bộ lọc tìm kiếm và phân trang
     * @return Trang kết quả chứa danh sách InventoryResponseDto của chi nhánh nhân
     *         viên
     */
    Page<InventoryResponseDto> getInventoriesByCurrentStaffBranch(InventoryFilterRequestForm filter);
}
