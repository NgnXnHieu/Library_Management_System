package com.library.service;

import com.library.dto.inventory.InventoryResponseDto;
import com.library.enums.DisplayStatus;
import com.library.requestform.inventory.InventoryFilterRequestForm;
import com.library.requestform.inventory.InventoryImportRequestForm;
import com.library.requestform.inventory.InventoryUpdateRequestForm;
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

    /**
     * Cập nhật thông tin bản ghi tồn kho (vị trí kệ, trạng thái hiển thị).
     *
     * @param id   ID bản ghi tồn kho
     * @param form Dữ liệu cập nhật
     * @return DTO tồn kho sau khi cập nhật
     */
    InventoryResponseDto updateInventory(Long id, InventoryUpdateRequestForm form);

    /**
     * Thay đổi trạng thái hiển thị của bản ghi tồn kho (HIDE / UNHIDE).
     *
     * @param id     ID bản ghi tồn kho
     * @param status Trạng thái mới
     * @return DTO tồn kho sau khi đổi trạng thái
     */
    InventoryResponseDto changeInventoryStatus(Long id, DisplayStatus status);

    /**
     * Nhập thêm số lượng sách vào kho chi nhánh.
     * Tự động cộng dồn số lượng vào cả tổng số lượng (totalQuantity) và số lượng khả dụng (availableQuantity).
     *
     * @param id   ID bản ghi tồn kho
     * @param form Form chứa số lượng nhập thêm
     * @return DTO tồn kho sau khi nhập thêm
     */
    InventoryResponseDto importStock(Long id, InventoryImportRequestForm form);
}
