package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.inventory.InventoryResponseDto;
import com.library.requestform.inventory.InventoryFilterRequestForm;
import com.library.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller tiếp nhận và xử lý các yêu cầu liên quan đến quản lý tồn kho sách
 * (Inventory).
 */
@RestController
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * API tự động khởi tạo danh sách tồn kho cho tất cả các đầu sách tại một chi
     * nhánh (Chỉ dành cho ADMIN).
     * Sẽ tự động quét toàn bộ sách hiện có và chỉ tạo mới cho các sách chưa có
     * trong kho của chi nhánh.
     *
     * @param branchId ID chi nhánh cần khởi tạo tồn kho
     * @return Danh sách DTO tồn kho vừa được tạo mới bọc trong chuẩn ApiResponse
     *         với mã HTTP 201
     */
    @PostMapping("/inventories/init/{branchId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<InventoryResponseDto>>> initInventories(
            @PathVariable Long branchId) {
        List<InventoryResponseDto> createdInventories = inventoryService.initInventoriesForBranch(branchId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Khởi tạo danh sách tồn kho cho chi nhánh thành công!", createdInventories));
    }

    /**
     * API công khai lấy danh sách tồn kho sách phân trang kèm theo bộ lọc tìm kiếm
     * và sắp xếp.
     * Sử dụng JOIN FETCH đa tầng để tối ưu hóa truy vấn và tránh N+1 query.
     *
     * @param filter Bộ lọc tìm kiếm và phân trang nhận qua Query Parameters
     * @return Trang kết quả chứa danh sách tồn kho bọc trong chuẩn ApiResponse
     */
    @GetMapping("/public/inventories")
    public ResponseEntity<ApiResponse<Page<InventoryResponseDto>>> getAllInventories(
            @ModelAttribute InventoryFilterRequestForm filter) {
        Page<InventoryResponseDto> inventories = inventoryService.getAllInventories(filter);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tồn kho sách thành công!", inventories));
    }

    /**
     * API lấy danh sách tồn kho sách phân trang theo chi nhánh của nhân viên đang
     * đăng nhập.
     * Chi nhánh được trích xuất tự động từ SecurityContextHolder.
     * Dành riêng cho BRANCHMANAGER và STAFF.
     *
     * @param filter Bộ lọc tìm kiếm và phân trang nhận qua Query Parameters
     * @return Trang kết quả chứa danh sách tồn kho của chi nhánh bọc trong chuẩn
     *         ApiResponse
     */
    @GetMapping("/inventories/current-branch")
    @PreAuthorize("hasAnyRole('BRANCHMANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<InventoryResponseDto>>> getMyBranchInventories(
            @ModelAttribute InventoryFilterRequestForm filter) {
        // Bước 1: Gọi xuống tầng Service để xử lý lấy tồn kho theo chi nhánh của nhân
        // viên
        Page<InventoryResponseDto> inventories = inventoryService.getInventoriesByCurrentStaffBranch(filter);

        // Bước 2: Bọc kết quả trả về trong chuẩn ApiResponse với HTTP status 200 OK
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tồn kho theo chi nhánh thành công!", inventories));
    }
}
