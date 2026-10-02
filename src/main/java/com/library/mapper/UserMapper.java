package com.library.mapper;

import com.library.dto.user.UserResponseDto;
import com.library.entity.User;
import com.library.requestform.account.RegisterRequestForm;
import com.library.requestform.user.UserUpdateRequestForm;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
public interface UserMapper {

    /**
     * Map từ RegisterRequestForm sang User entity
     */
    User toEntity(RegisterRequestForm form);

    /**
     * Map từ User entity sang UserResponseDto
     * Tự động lấy thông tin từ quan hệ Account, Role, Branch
     */
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "username", source = "account.username")
    @Mapping(target = "role", source = "role.code")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    UserResponseDto toDto(User user);

    /**
     * Map danh sách User sang danh sách UserResponseDto
     */
    List<UserResponseDto> toDtoList(List<User> users);

    /**
     * Cập nhật thông tin User hiện có từ UserUpdateRequestForm
     * Bỏ qua trường 'role' vì logic gán Role đã được xử lý và kiểm tra quyền riêng trong Service
     * Các trường null trong form sẽ tự động được bỏ qua, giữ nguyên dữ liệu cũ
     */
    @Mapping(target = "role", ignore = true)
    void updateEntityFromForm(UserUpdateRequestForm form, @MappingTarget User user);
}
