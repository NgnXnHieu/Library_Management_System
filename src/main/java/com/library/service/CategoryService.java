package com.library.service;

import com.library.dto.category.CategoryResponseDto;
import com.library.enums.DisplayStatus;
import com.library.requestform.category.CategoryCreateRequestForm;
import com.library.requestform.category.CategoryFilterRequestForm;
import com.library.requestform.category.CategoryUpdateRequestForm;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interface định nghĩa các nghiệp vụ quản lý thể loại sách.
 */
public interface CategoryService {

    /**
     * Thêm mới thể loại sách (Dành cho ADMIN).
     *
     * @param form Dữ liệu tạo mới thể loại
     * @return DTO thông tin thể loại vừa tạo
     */
    CategoryResponseDto createCategory(CategoryCreateRequestForm form);

    /**
     * Cập nhật thông tin thể loại sách (Dành cho ADMIN).
     *
     * @param id   ID của thể loại cần cập nhật
     * @param form Dữ liệu cập nhật
     * @return DTO thông tin thể loại sau khi cập nhật
     */
    CategoryResponseDto updateCategory(Long id, CategoryUpdateRequestForm form);

    /**
     * Xóa thể loại sách khỏi hệ thống (Dành cho ADMIN).
     *
     * @param id ID của thể loại cần xóa
     */
    void deleteCategory(Long id);

    /**
     * Lấy thông tin chi tiết một thể loại theo ID.
     *
     * @param id ID của thể loại
     * @return DTO thông tin thể loại
     */
    CategoryResponseDto getCategoryById(Long id);

    /**
     * Lấy danh sách toàn bộ các thể loại trong hệ thống.
     *
     * @return Danh sách DTO các thể loại
     */
    List<CategoryResponseDto> getAllCategories();

    /**
     * Lấy danh sách phân trang các thể loại kèm theo bộ lọc tìm kiếm và sắp xếp (Dành cho ADMIN).
     *
     * @param filter Bộ lọc tìm kiếm và thông tin phân trang (name, description, status, page, size, sortBy, sortDir)
     * @return Trang kết quả chứa danh sách CategoryResponseDto
     */
    Page<CategoryResponseDto> getCategoriesWithFilter(CategoryFilterRequestForm filter);

    /**
     * Lấy danh sách toàn bộ các giá trị enum trạng thái hiển thị của thể loại (DisplayStatus: HIDE, UNHIDE).
     *
     * @return Danh sách các enum DisplayStatus
     */
    List<DisplayStatus> getDisplayStatuses();
}
