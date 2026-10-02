package com.library.service;

import com.library.dto.branch.BranchResponseDto;
import com.library.enums.BranchStatus;
import com.library.requestform.branch.BranchCreateRequestForm;
import com.library.requestform.branch.BranchFilterRequestForm;
import com.library.requestform.branch.BranchUpdateRequestForm;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interface định nghĩa các nghiệp vụ quản lý chi nhánh thư viện.
 */
public interface BranchService {

    /**
     * Thêm mới chi nhánh (chỉ dành cho ADMIN)
     */
    BranchResponseDto createBranch(BranchCreateRequestForm form);

    /**
     * Cập nhật thông tin chi nhánh (chỉ dành cho ADMIN)
     */
    BranchResponseDto updateBranch(Long id, BranchUpdateRequestForm form);

    /**
     * Xóa chi nhánh (chỉ dành cho ADMIN)
     */
    void deleteBranch(Long id);

    /**
     * Lấy thông tin chi nhánh theo ID
     */
    BranchResponseDto getBranchById(Long id);

    /**
     * Lấy danh sách toàn bộ chi nhánh
     */
    List<BranchResponseDto> getAllBranches();

    /**
     * Lấy danh sách phân trang chi nhánh kèm bộ lọc tìm kiếm và sắp xếp.
     * Mặc định sắp xếp theo createdAt với thời gian mới nhất lên đầu.
     *
     * @param filter Bộ lọc tìm kiếm và phân trang
     * @return Trang kết quả chứa danh sách DTO chi nhánh
     */
    Page<BranchResponseDto> getBranchesWithFilter(BranchFilterRequestForm filter);

    /**
     * Lấy danh sách toàn bộ các giá trị trạng thái hoạt động của chi nhánh (BranchStatus).
     *
     * @return Danh sách các enum BranchStatus
     */
    List<BranchStatus> getBranchStatuses();
}
