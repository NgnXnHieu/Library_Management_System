package com.library.service.impl;

import com.library.dto.role.RoleResponseDto;
import com.library.entity.Role;
import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.RoleMapper;
import com.library.repository.RoleRepository;
import com.library.requestform.role.RoleCreateRequestForm;
import com.library.requestform.role.RoleUpdateRequestForm;
import com.library.service.RoleService;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    public RoleResponseDto createRole(RoleCreateRequestForm form) {
        String code = form.getCode().trim().toUpperCase();

        // 1. Kiểm tra mã vai trò đã tồn tại chưa
        if (roleRepository.existsByCode(code)) {
            throw new BadRequestException("Mã vai trò '" + code + "' đã tồn tại trên hệ thống!");
        }

        // 2. Dùng MapStruct map từ Form sang Entity
        Role role = roleMapper.toEntity(form);
        role.setCode(code); // Chuẩn hóa mã chữ hoa

        Role savedRole = roleRepository.save(role);

        // 3. Dùng MapStruct map từ Entity sang DTO
        return roleMapper.toDto(savedRole);
    }

    @Override
    @Transactional
    public RoleResponseDto updateRole(Long id, RoleUpdateRequestForm form) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò với ID: " + id));

        String code = form.getCode().trim().toUpperCase();

        // Nếu mã vai trò thay đổi, kiểm tra trùng lặp với các role khác
        if (!role.getCode().equalsIgnoreCase(code) && roleRepository.existsByCodeAndIdNot(code, id)) {
            throw new BadRequestException("Mã vai trò '" + code + "' đã được sử dụng bởi vai trò khác!");
        }

        // Dùng MapStruct cập nhật dữ liệu từ Form vào Entity hiện có
        roleMapper.updateEntityFromForm(form, role);
        role.setCode(code); // Giữ mã chữ hoa đã chuẩn hóa

        Role updatedRole = roleRepository.save(role);
        return roleMapper.toDto(updatedRole);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò với ID: " + id));

        // Ràng buộc nghiệp vụ: Không cho phép xóa vai trò nếu đang có người dùng thuộc vai trò này
        if (userRepository.existsByRoleId(id)) {
            throw new BadRequestException("Không thể xóa vai trò '" + role.getName() + "' vì đang có người dùng được gán vai trò này!");
        }

        roleRepository.delete(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponseDto getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò với ID: " + id));
        return roleMapper.toDto(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponseDto> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        // Dùng MapStruct map danh sách Entity sang danh sách DTO
        return roleMapper.toDtoList(roles);
    }
}
