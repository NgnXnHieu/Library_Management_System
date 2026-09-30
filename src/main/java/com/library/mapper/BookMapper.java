package com.library.mapper;

import com.library.dto.book.BookResponseDto;
import com.library.entity.Book;
import com.library.entity.Category;
import com.library.requestform.book.BookCreateRequestForm;
import com.library.requestform.book.BookUpdateRequestForm;
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
 * Mapper chuyển đổi giữa Entity Book, DTO và Form bằng MapStruct.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface BookMapper {

    /**
     * Map từ Form tạo mới và Category Entity sang Entity Book.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "status", source = "form.status")
    @Mapping(target = "description", source = "form.description")
    Book toEntity(BookCreateRequestForm form, Category category);

    /**
     * Map từ Entity Book sang DTO BookResponseDto.
     */
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "coverImageUrl", source = "coverImageKey", qualifiedByName = "mapCoverImageUrl")
    BookResponseDto toDto(Book book);

    /**
     * Map danh sách Entity Book sang danh sách DTO.
     */
    List<BookResponseDto> toDtoList(List<Book> books);

    /**
     * Cập nhật thông tin Entity Book từ Form cập nhật.
     * Các trường mang giá trị null trong form sẽ tự động được bỏ qua nhờ NullValuePropertyMappingStrategy.IGNORE.
     * Trường category và isbn được bỏ qua để xử lý kiểm tra logic nghiệp vụ riêng tại Service.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "isbn", ignore = true)
    @Mapping(target = "inventories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromForm(BookUpdateRequestForm form, @MappingTarget Book book);

    @Named("mapCoverImageUrl")
    default String mapCoverImageUrl(String coverImageKey) {
        return FileUtil.buildFileUrl(coverImageKey);
    }
}
