package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.role.RoleResponseDto;
import com.library.requestform.role.RoleCreateRequestForm;
import com.library.requestform.role.RoleUpdateRequestForm;
import com.library.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponseDto>> createRole(
            @Valid @RequestBody RoleCreateRequestForm form) {
        RoleResponseDto createdRole = roleService.createRole(form);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm mới vai trò thành công!", createdRole));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponseDto>>> getAllRoles() {
        List<RoleResponseDto> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách vai trò thành công!", roles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponseDto>> getRoleById(@PathVariable Long id) {
        RoleResponseDto role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin vai trò thành công!", role));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponseDto>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequestForm form) {
        RoleResponseDto updatedRole = roleService.updateRole(id, form);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật vai trò thành công!", updatedRole));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa vai trò thành công!", null));
    }
}
