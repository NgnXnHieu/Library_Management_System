package com.library.service.impl;

import com.library.dto.category.CategoryResponseDto;
import com.library.entity.Category;
import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import com.library.mapper.CategoryMapper;
import com.library.repository.BookRepository;
import com.library.repository.CategoryRepository;
import com.library.requestform.category.CategoryCreateRequestForm;
import com.library.requestform.category.CategoryUpdateRequestForm;
import com.library.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service triển khai các nghiệp vụ quản lý thể loại sách.
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Thêm mới thể loại sách (Dành cho ADMIN).
     *
     * @param form Dữ liệu tạo mới thể loại
     * @return DTO thông tin thể loại vừa tạo
     */
    @Override
    @Transactional
    public CategoryResponseDto createCategory(CategoryCreateRequestForm form) {
        String name = form.getName().trim();

        // Bước 1: Kiểm tra tên thể loại đã tồn tại trong hệ thống chưa
        if (categoryRepository.existsByName(name)) {
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS, name);
        }

        // Bước 2: Chuyển đổi dữ liệu từ Form sang Entity và chuẩn hóa tên
        Category category = categoryMapper.toEntity(form);
        category.setName(name);

        // Chuẩn hóa mô tả nếu rỗng
        if (form.getDescription() != null && form.getDescription().trim().isEmpty()) {
            category.setDescription(null);
        }

        // Bước 3: Lưu thể loại mới vào Database
        Category savedCategory = categoryRepository.save(category);

        // Bước 4: Chuyển đổi sang DTO và trả về kết quả
        return categoryMapper.toDto(savedCategory);
    }

    /**
     * Cập nhật thông tin thể loại sách (Dành cho ADMIN).
     *
     * @param id   ID của thể loại cần cập nhật
     * @param form Dữ liệu cập nhật
     * @return DTO thông tin thể loại sau khi cập nhật
     */
    @Override
    @Transactional
    public CategoryResponseDto updateCategory(Long id, CategoryUpdateRequestForm form) {
        // Bước 1: Tìm kiếm thể loại theo ID, ném ngoại lệ nếu không tìm thấy
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND, id));

        // Bước 2: Nếu tên thay đổi, kiểm tra trùng lặp với thể loại khác trong hệ thống
        if (form.getName() != null && !form.getName().trim().isEmpty()) {
            String newName = form.getName().trim();
            if (!category.getName().equalsIgnoreCase(newName) && categoryRepository.existsByNameAndIdNot(newName, id)) {
                throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS, newName);
            }
            category.setName(newName);
        }

        // Bước 3: Cập nhật các trường còn lại từ form vào entity qua MapStruct
        categoryMapper.updateEntityFromForm(form, category);

        // Chuẩn hóa mô tả nếu form gửi chuỗi rỗng
        if (form.getDescription() != null && form.getDescription().trim().isEmpty()) {
            category.setDescription(null);
        }

        // Bước 4: Lưu thay đổi vào Database
        Category updatedCategory = categoryRepository.save(category);

        // Bước 5: Chuyển đổi sang DTO và trả về kết quả
        return categoryMapper.toDto(updatedCategory);
    }

    /**
     * Xóa thể loại sách khỏi hệ thống (Dành cho ADMIN).
     *
     * @param id ID của thể loại cần xóa
     */
    @Override
    @Transactional
    public void deleteCategory(Long id) {
        // Bước 1: Tìm kiếm thể loại theo ID, ném ngoại lệ nếu không tìm thấy
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND, id));

        // Bước 2: Kiểm tra ràng buộc toàn vẹn dữ liệu (không xóa nếu đang có sách thuộc thể loại này)
        if (bookRepository.existsByCategoryId(id)) {
            throw new AppException(ErrorCode.CATEGORY_CANNOT_DELETE, category.getName());
        }

        // Bước 3: Thực hiện xóa cứng thể loại khỏi Database
        categoryRepository.delete(category);
    }

    /**
     * Lấy thông tin chi tiết một thể loại theo ID.
     *
     * @param id ID của thể loại
     * @return DTO thông tin thể loại
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Long id) {
        // Bước 1: Tìm kiếm thể loại theo ID
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND, id));

        // Bước 2: Chuyển đổi sang DTO và trả về
        return categoryMapper.toDto(category);
    }

    /**
     * Lấy danh sách toàn bộ các thể loại trong hệ thống.
     *
     * @return Danh sách DTO các thể loại
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllCategories() {
        // Bước 1: Lấy toàn bộ danh sách thể loại từ Database
        List<Category> categories = categoryRepository.findAll();

        // Bước 2: Chuyển đổi sang danh sách DTO và trả về
        return categoryMapper.toDtoList(categories);
    }
}
