package com.library.service;

import com.library.dto.role.RoleResponseDto;
import com.library.requestform.role.RoleCreateRequestForm;
import com.library.requestform.role.RoleUpdateRequestForm;

import java.util.List;

public interface RoleService {

    RoleResponseDto createRole(RoleCreateRequestForm form);

    RoleResponseDto updateRole(Long id, RoleUpdateRequestForm form);

    void deleteRole(Long id);

    RoleResponseDto getRoleById(Long id);

    List<RoleResponseDto> getAllRoles();
}
