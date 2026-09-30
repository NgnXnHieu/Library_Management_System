package com.library.service;

import com.library.dto.branch.BranchResponseDto;
import com.library.requestform.branch.BranchCreateRequestForm;
import com.library.requestform.branch.BranchUpdateRequestForm;

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
}
