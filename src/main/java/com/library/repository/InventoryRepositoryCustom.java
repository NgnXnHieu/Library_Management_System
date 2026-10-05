package com.library.repository;

import com.library.dto.inventory.InventoryResponseDto;
import com.library.requestform.inventory.InventoryFilterRequestForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface mở rộng cho InventoryRepository định nghĩa các truy vấn Native SQL tùy biến.
 */
public interface InventoryRepositoryCustom {

    /**
     * Lấy danh sách tồn kho sách phân trang kèm bộ lọc tìm kiếm và sắp xếp động.
     * Sử dụng Native SQL Query và @SqlResultSetMapping "InventoryPageResponseMapping".
     *
     * @param filter   Bộ lọc tìm kiếm tồn kho
     * @param pageable Thông tin phân trang và sắp xếp
     * @return Trang kết quả chứa danh sách InventoryResponseDto
     */
    Page<InventoryResponseDto> findAllInventoriesNative(InventoryFilterRequestForm filter, Pageable pageable);
}
