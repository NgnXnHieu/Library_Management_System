package com.library.mapper;

import com.library.dto.category.CategoryResponseDto;
import com.library.entity.Category;
import com.library.requestform.category.CategoryCreateRequestForm;
import com.library.requestform.category.CategoryUpdateRequestForm;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper chuyển đổi giữa Entity Category, DTO và Form bằng MapStruct.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface CategoryMapper {

    /**
     * Map từ Form tạo mới sang Entity Category
     */
    Category toEntity(CategoryCreateRequestForm form);

    /**
     * Map từ Entity Category sang DTO CategoryResponseDto
     */
    CategoryResponseDto toDto(Category category);

    /**
     * Map danh sách Entity Category sang danh sách DTO
     */
    List<CategoryResponseDto> toDtoList(List<Category> categories);

    /**
     * Cập nhật thông tin Category hiện có từ CategoryUpdateRequestForm
     * Các trường null trong form sẽ tự động được bỏ qua
     */
    void updateEntityFromForm(CategoryUpdateRequestForm form, @MappingTarget Category category);
}
