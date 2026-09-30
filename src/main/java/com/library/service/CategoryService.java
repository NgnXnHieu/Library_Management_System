package com.library.service;

import com.library.dto.category.CategoryResponseDto;
import com.library.requestform.category.CategoryCreateRequestForm;
import com.library.requestform.category.CategoryUpdateRequestForm;

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
}
