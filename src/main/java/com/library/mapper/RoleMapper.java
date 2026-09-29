package com.library.mapper;

import com.library.dto.role.RoleResponseDto;
import com.library.entity.Role;
import com.library.requestform.role.RoleCreateRequestForm;
import com.library.requestform.role.RoleUpdateRequestForm;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface RoleMapper {

    /**
     * Map từ Form tạo mới sang Entity Role
     */
    Role toEntity(RoleCreateRequestForm form);

    /**
     * Map từ Entity Role sang DTO phản hồi RoleResponseDto
     */
    RoleResponseDto toDto(Role role);

    /**
     * Map danh sách Entity sang danh sách DTO
     */
    List<RoleResponseDto> toDtoList(List<Role> roles);

    /**
     * Cập nhật thông tin Role hiện có từ RoleUpdateRequestForm
     * Các trường null trong form sẽ tự động được bỏ qua, giữ nguyên dữ liệu cũ
     */
    void updateEntityFromForm(RoleUpdateRequestForm form, @MappingTarget Role role);
}
