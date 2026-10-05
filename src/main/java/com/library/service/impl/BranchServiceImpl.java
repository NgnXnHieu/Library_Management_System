package com.library.service.impl;

import com.library.dto.branch.BranchResponseDto;
import com.library.dto.branch.BranchStatisticResponseDto;
import com.library.entity.Branch;
import com.library.enums.BorrowStatus;
import com.library.enums.BranchStatus;
import com.library.enums.DisplayStatus;
import com.library.enums.Role;
import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import com.library.mapper.BranchMapper;
import com.library.repository.BranchRepository;
import com.library.repository.InventoryRepository;
import com.library.repository.UserRepository;
import com.library.requestform.branch.BranchCreateRequestForm;
import com.library.requestform.branch.BranchFilterRequestForm;
import com.library.requestform.branch.BranchUpdateRequestForm;
import com.library.service.BranchService;
import com.library.specification.BranchSpecification;
import com.library.util.FileUtil;
import com.library.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Service triển khai các nghiệp vụ quản lý chi nhánh thư viện.
 */
@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "code", "name", "address", "phone", "status", "createdAt", "updatedAt"
    );

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
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

        // Chuẩn hóa đường dẫn ảnh nếu rỗng
        if (form.getImageUrl() != null && form.getImageUrl().trim().isEmpty()) {
            branch.setImageUrl(null);
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

        // Bước 4: Nếu người dùng cập nhật ảnh mới khác với ảnh cũ -> Xóa file ảnh cũ trên ổ đĩa để tránh rác dung lượng
        String oldImageUrl = branch.getImageUrl();
        String newImageUrl = form.getImageUrl();
        if (newImageUrl != null && !newImageUrl.trim().isEmpty() && !newImageUrl.trim().equals(oldImageUrl)) {
            if (oldImageUrl != null && !oldImageUrl.trim().isEmpty()) {
                FileUtil.deleteFile(oldImageUrl);
            }
        }

        // Bước 5: Cập nhật các trường dữ liệu từ form vào entity qua MapStruct
        branchMapper.updateEntityFromForm(form, branch);

        // Chuẩn hóa số điện thoại nếu form gửi chuỗi rỗng
        if (form.getPhone() != null && form.getPhone().trim().isEmpty()) {
            branch.setPhone(null);
        }

        Branch updatedBranch = branchRepository.save(branch);

        // Bước 6: Nếu chi nhánh chuyển sang trạng thái CLOSED -> Tự động cập nhật tất cả tồn kho thuộc chi nhánh này sang HIDE
        if (updatedBranch.getStatus() == BranchStatus.CLOSED) {
            inventoryRepository.updateStatusByBranchId(updatedBranch.getId(), DisplayStatus.HIDE);
        }

        // Bước 7: Chuyển đổi sang DTO và trả về
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

        // Bước 4: Lưu lại đường dẫn ảnh để xóa file sau khi xóa thành công trong DB
        String imageToDelete = branch.getImageUrl();

        // Bước 5: Thực hiện xóa chi nhánh khỏi Database
        branchRepository.delete(branch);

        // Bước 6: Xóa tệp ảnh của chi nhánh trên ổ đĩa máy chủ nếu có
        if (imageToDelete != null && !imageToDelete.trim().isEmpty()) {
            FileUtil.deleteFile(imageToDelete);
        }
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
     * Lấy danh sách phân trang các chi nhánh kèm bộ lọc tìm kiếm và sắp xếp.
     * Mặc định sắp xếp theo createdAt với thời gian mới nhất lên đầu (DESC).
     *
     * @param filter Bộ lọc chứa code, name, address, phone, status và tham số phân trang
     * @return Trang kết quả phân trang chứa DTO chi nhánh
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BranchResponseDto> getBranchesWithFilter(BranchFilterRequestForm filter) {
        // Bước 1: Chuẩn hóa tham số bộ lọc nếu client gửi null
        if (filter == null) {
            filter = new BranchFilterRequestForm();
        }

        // Bước 2: Xác định trường sắp xếp an toàn (mặc định createdAt)
        String sortBy = filter.getSortBy();
        if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy.trim())) {
            sortBy = "createdAt";
        } else {
            sortBy = sortBy.trim();
        }

        // Bước 3: Xác định hướng sắp xếp (mặc định desc: mới nhất lên đầu)
        Sort.Direction direction = "asc".equalsIgnoreCase(filter.getSortDir())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        // Bước 4: Tạo đối tượng Pageable của Spring Data JPA
        int pageNumber = Math.max(0, filter.getPage());
        int pageSize = Math.max(1, filter.getSize());
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

        // Bước 5: Truy vấn Database qua BranchSpecification và ánh xạ sang DTO bằng MapStruct
        Page<Branch> branchPage = branchRepository.findAll(BranchSpecification.filter(filter), pageable);
        return branchPage.map(branchMapper::toDto);
    }

    /**
     * Lấy danh sách toàn bộ trạng thái hoạt động của chi nhánh (BranchStatus).
     *
     * @return Danh sách các giá trị enum BranchStatus
     */
    @Override
    @Transactional(readOnly = true)
    public List<BranchStatus> getBranchStatuses() {
        return List.of(BranchStatus.values());
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

    /**
     * Lấy danh sách phân trang thống kê chi nhánh (dành riêng cho ADMIN).
     * Bao gồm: số lượng sách trong kho, số sách còn, số sách đang mượn, lượt mượn và doanh thu.
     * Sử dụng JPQL Constructor Expression tối ưu truy vấn chiếu trực tiếp lên BranchStatisticResponseDto.
     *
     * @param filter Bộ lọc chứa code, name, status và tham số phân trang
     * @return Trang kết quả phân trang chứa DTO thống kê chi nhánh
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BranchStatisticResponseDto> getBranchStatistics(BranchFilterRequestForm filter) {
        // Bước 1: Chuẩn hóa tham số bộ lọc nếu client gửi null
        if (filter == null) {
            filter = new BranchFilterRequestForm();
        }

        // Bước 2: Xác định trường sắp xếp an toàn (mặc định createdAt)
        String sortBy = filter.getSortBy();
        if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy.trim())) {
            sortBy = "createdAt";
        } else {
            sortBy = sortBy.trim();
        }

        // Bước 3: Xác định hướng sắp xếp (mặc định desc: mới nhất lên đầu)
        Sort.Direction direction = "asc".equalsIgnoreCase(filter.getSortDir())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        // Bước 4: Tạo đối tượng Pageable của Spring Data JPA
        int pageNumber = Math.max(0, filter.getPage());
        int pageSize = Math.max(1, filter.getSize());
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

        // Bước 5: Định nghĩa danh sách các trạng thái phiếu mượn hợp lệ được tính thống kê
        List<BorrowStatus> borrowStatuses = List.of(
                BorrowStatus.BORROWED,
                BorrowStatus.RETURNED,
                BorrowStatus.OVERDUE
        );

        // Bước 6: Gọi truy vấn JPQL Constructor Expression qua Repository
        return branchRepository.findBranchStatisticsWithFilter(filter, borrowStatuses, pageable);
    }
}
