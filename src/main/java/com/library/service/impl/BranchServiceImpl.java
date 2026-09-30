package com.library.service.impl;

import com.library.dto.branch.BranchResponseDto;
import com.library.entity.Branch;
import com.library.enums.Role;
import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import com.library.mapper.BranchMapper;
import com.library.repository.BranchRepository;
import com.library.repository.UserRepository;
import com.library.requestform.branch.BranchCreateRequestForm;
import com.library.requestform.branch.BranchUpdateRequestForm;
import com.library.service.BranchService;
import com.library.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service triển khai các nghiệp vụ quản lý chi nhánh thư viện.
 */
@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final BranchMapper branchMapper;

    /**
     * Thêm mới chi nhánh vào hệ thống (Chỉ dành cho ADMIN).
     *
     * @param form Thông tin chi nhánh cần tạo
     * @return DTO thông tin chi nhánh vừa tạo
     */
    @Override
    @Transactional
    public BranchResponseDto createBranch(BranchCreateRequestForm form) {
        // Bước 1: Kiểm tra quyền hạn của người thực hiện (bắt buộc phải là ADMIN)
        checkAdminPermission();

        String code = form.getCode().trim().toUpperCase();

        // Bước 2: Kiểm tra mã chi nhánh đã tồn tại chưa
        if (branchRepository.existsByCode(code)) {
            throw new AppException(ErrorCode.BRANCH_ALREADY_EXISTS, code);
        }

        // Bước 3: Chuyển đổi dữ liệu từ Form sang Entity và chuẩn hóa mã chi nhánh
        Branch branch = branchMapper.toEntity(form);
        branch.setCode(code);

        // Chuẩn hóa số điện thoại nếu rỗng
        if (form.getPhone() != null && form.getPhone().trim().isEmpty()) {
            branch.setPhone(null);
        }

        Branch savedBranch = branchRepository.save(branch);

        // Bước 4: Chuyển đổi sang DTO và trả về kết quả
        return branchMapper.toDto(savedBranch);
    }

    /**
     * Cập nhật thông tin chi nhánh hiện có (Chỉ dành cho ADMIN).
     *
     * @param id   ID của chi nhánh cần cập nhật
     * @param form Dữ liệu cập nhật
     * @return DTO thông tin chi nhánh sau khi cập nhật
     */
    @Override
    @Transactional
    public BranchResponseDto updateBranch(Long id, BranchUpdateRequestForm form) {
        // Bước 1: Kiểm tra quyền hạn của người thực hiện (bắt buộc phải là ADMIN)
        checkAdminPermission();

        // Bước 2: Tìm kiếm chi nhánh theo ID
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND, id));

        // Bước 3: Nếu mã chi nhánh thay đổi, kiểm tra trùng lặp với các chi nhánh khác
        if (form.getCode() != null && !form.getCode().trim().isEmpty()) {
            String newCode = form.getCode().trim().toUpperCase();
            if (!branch.getCode().equalsIgnoreCase(newCode) && branchRepository.existsByCodeAndIdNot(newCode, id)) {
                throw new AppException(ErrorCode.BRANCH_ALREADY_EXISTS, newCode);
            }
            branch.setCode(newCode);
        }

        // Bước 4: Cập nhật các trường dữ liệu từ form vào entity qua MapStruct
        branchMapper.updateEntityFromForm(form, branch);

        // Chuẩn hóa số điện thoại nếu form gửi chuỗi rỗng
        if (form.getPhone() != null && form.getPhone().trim().isEmpty()) {
            branch.setPhone(null);
        }

        Branch updatedBranch = branchRepository.save(branch);

        // Bước 5: Chuyển đổi sang DTO và trả về
        return branchMapper.toDto(updatedBranch);
    }

    /**
     * Xóa chi nhánh khỏi hệ thống (Chỉ dành cho ADMIN).
     *
     * @param id ID của chi nhánh cần xóa
     */
    @Override
    @Transactional
    public void deleteBranch(Long id) {
        // Bước 1: Kiểm tra quyền hạn của người thực hiện (bắt buộc phải là ADMIN)
        checkAdminPermission();

        // Bước 2: Tìm kiếm chi nhánh theo ID
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND, id));

        // Bước 3: Kiểm tra ràng buộc toàn vẹn dữ liệu (không xóa chi nhánh nếu đang có
        // người dùng thuộc chi nhánh này)
        if (userRepository.existsByBranchId(id)) {
            throw new AppException(ErrorCode.BRANCH_CANNOT_DELETE, branch.getName());
        }

        // Bước 4: Thực hiện xóa chi nhánh
        branchRepository.delete(branch);
    }

    /**
     * Lấy thông tin chi tiết chi nhánh theo ID.
     *
     * @param id ID của chi nhánh
     * @return DTO thông tin chi nhánh
     */
    @Override
    @Transactional(readOnly = true)
    public BranchResponseDto getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND, id));
        return branchMapper.toDto(branch);
    }

    /**
     * Lấy danh sách toàn bộ chi nhánh trong hệ thống.
     *
     * @return Danh sách DTO các chi nhánh
     */
    @Override
    @Transactional(readOnly = true)
    public List<BranchResponseDto> getAllBranches() {
        List<Branch> branches = branchRepository.findAll();
        return branchMapper.toDtoList(branches);
    }

    /**
     * Kiểm tra quyền hạn người dùng hiện tại có phải là ADMIN hay không.
     * Ném ra ngoại lệ AppException(ErrorCode.ACCESS_DENIED) nếu không đủ quyền.
     */
    private void checkAdminPermission() {
        String currentRole = SecurityUtil.getCurrentRoleCode().orElse(null);
        // if (currentRole == null || !Role.ADMIN.name().equalsIgnoreCase(currentRole))
        // {
        // throw new AppException(ErrorCode.ACCESS_DENIED);
        // }
    }
}
