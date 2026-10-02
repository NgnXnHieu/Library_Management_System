package com.library.mapper;

import com.library.dto.inventory.InventoryResponseDto;
import com.library.entity.Inventory;
import com.library.util.FileUtil;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper chuyển đổi giữa Entity Inventory và DTO bằng MapStruct.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface InventoryMapper {

    /**
     * Map từ Entity Inventory sang DTO InventoryResponseDto.
     */
    @Mapping(source = "branch.id", target = "branchId")
    @Mapping(source = "branch.name", target = "branchName")
    @Mapping(source = "book.id", target = "bookId")
    @Mapping(source = "book.title", target = "bookTitle")
    @Mapping(source = "book.isbn", target = "isbn")
    @Mapping(source = "book.coverImageKey", target = "coverImageUrl", qualifiedByName = "mapCoverImageUrl")
    @Mapping(source = "book.category.id", target = "categoryId")
    @Mapping(source = "book.category.name", target = "categoryName")
    @Mapping(source = "book.price", target = "price")
    @Mapping(source = "book.rentalPrice", target = "rentalPrice")
    @Mapping(source = "book.fineAmount", target = "fineAmount")
    InventoryResponseDto toDto(Inventory inventory);

    /**
     * Map danh sách Entity Inventory sang danh sách DTO.
     */
    List<InventoryResponseDto> toDtoList(List<Inventory> inventories);

    /**
     * Chuyển đổi coverImageKey thành đường dẫn URL xem ảnh đầy đủ
     */
    @Named("mapCoverImageUrl")
    default String mapCoverImageUrl(String coverImageKey) {
        return FileUtil.buildFileUrl(coverImageKey);
    }
}
