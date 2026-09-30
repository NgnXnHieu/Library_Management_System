package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.branch.BranchResponseDto;
import com.library.requestform.branch.BranchCreateRequestForm;
import com.library.requestform.branch.BranchUpdateRequestForm;
import com.library.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller tiếp nhận và xử lý các yêu cầu liên quan đến chi nhánh (Branch).
 */
@RestController
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    /**
     * API thêm mới chi nhánh (Yêu cầu quyền ADMIN).
     *
     * @param form Dữ liệu tạo chi nhánh: code, name, address, status (bắt buộc, BranchStatus: OPEN, CLOSED) và phone (tùy chọn)
     * @return DTO chi nhánh vừa tạo bọc trong chuẩn ApiResponse với HTTP 201
     */
    @PostMapping("/branches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponseDto>> createBranch(
            @Valid @RequestBody BranchCreateRequestForm form) {
        BranchResponseDto createdBranch = branchService.createBranch(form);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm mới chi nhánh thành công!", createdBranch));
    }

    /**
     * API cập nhật thông tin chi nhánh (Yêu cầu quyền ADMIN).
     *
     * @param id ID của chi nhánh cần cập nhật
     * @param form Dữ liệu cập nhật
     * @return DTO chi nhánh sau khi cập nhật
     */
    @PutMapping("/branches/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponseDto>> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchUpdateRequestForm form) {
        BranchResponseDto updatedBranch = branchService.updateBranch(id, form);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chi nhánh thành công!", updatedBranch));
    }

    /**
     * API xóa chi nhánh (Yêu cầu quyền ADMIN).
     *
     * @param id ID của chi nhánh cần xóa
     * @return Phản hồi thành công
     */
    @DeleteMapping("/branches/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa chi nhánh thành công!", null));
    }

    /**
     * API lấy thông tin chi tiết chi nhánh theo ID (Cho phép mọi tài khoản đã đăng nhập).
     *
     * @param id ID của chi nhánh
     * @return DTO chi tiết chi nhánh
     */
    @GetMapping("/branches/{id}")
    public ResponseEntity<ApiResponse<BranchResponseDto>> getBranchById(@PathVariable Long id) {
        BranchResponseDto branch = branchService.getBranchById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi nhánh thành công!", branch));
    }

    /**
     * API lấy danh sách toàn bộ các chi nhánh (Cho phép mọi tài khoản đã đăng nhập).
     *
     * @return Danh sách chi nhánh
     */
    @GetMapping("/branches")
    public ResponseEntity<ApiResponse<List<BranchResponseDto>>> getAllBranches() {
        List<BranchResponseDto> branches = branchService.getAllBranches();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chi nhánh thành công!", branches));
    }

    /**
     * API công khai lấy danh sách chi nhánh (Không cần đăng nhập, tiền tố /public).
     *
     * @return Danh sách chi nhánh
     */
    @GetMapping("/public/branches")
    public ResponseEntity<ApiResponse<List<BranchResponseDto>>> getPublicBranches() {
        List<BranchResponseDto> branches = branchService.getAllBranches();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chi nhánh thành công!", branches));
    }
}
