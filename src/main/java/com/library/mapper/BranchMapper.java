package com.library.mapper;

import com.library.dto.branch.BranchResponseDto;
import com.library.entity.Branch;
import com.library.requestform.branch.BranchCreateRequestForm;
import com.library.requestform.branch.BranchUpdateRequestForm;
import com.library.util.FileUtil;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper chuyển đổi giữa Entity Branch, DTO và Form bằng MapStruct.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface BranchMapper {

    /**
     * Map từ Form tạo mới sang Entity Branch
     */
    Branch toEntity(BranchCreateRequestForm form);

    /**
     * Map từ Entity Branch sang DTO BranchResponseDto
     */
    @Mapping(target = "imageUrl", source = "imageUrl", qualifiedByName = "mapImageUrl")
    BranchResponseDto toDto(Branch branch);

    /**
     * Map danh sách Entity Branch sang danh sách DTO
     */
    List<BranchResponseDto> toDtoList(List<Branch> branches);

    /**
     * Cập nhật thông tin Branch hiện có từ BranchUpdateRequestForm
     * Các trường null trong form sẽ tự động được bỏ qua
     */
    void updateEntityFromForm(BranchUpdateRequestForm form, @MappingTarget Branch branch);

    @Named("mapImageUrl")
    default String mapImageUrl(String imageUrl) {
        return FileUtil.buildFileUrl(imageUrl);
    }
}
