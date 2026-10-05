package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.category.CategoryResponseDto;
import com.library.dto.category.CategorySimpleDto;
import com.library.enums.DisplayStatus;
import com.library.requestform.category.CategoryCreateRequestForm;
import com.library.requestform.category.CategoryFilterRequestForm;
import com.library.requestform.category.CategoryUpdateRequestForm;
import com.library.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller tiếp nhận và xử lý các yêu cầu liên quan đến thể loại sách (Category).
 */
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * API thêm mới thể loại sách (Yêu cầu quyền ADMIN).
     *
     * @param form Dữ liệu tạo thể loại: name, status (bắt buộc, DisplayStatus: HIDE, UNHIDE) và description (tùy chọn)
     * @return DTO thể loại vừa tạo bọc trong chuẩn ApiResponse với HTTP 201
     */
    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(
            @Valid @RequestBody CategoryCreateRequestForm form) {
        CategoryResponseDto createdCategory = categoryService.createCategory(form);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm mới thể loại thành công!", createdCategory));
    }

    /**
     * API cập nhật thông tin thể loại sách (Yêu cầu quyền ADMIN).
     *
     * @param id   ID của thể loại cần cập nhật
     * @param form Dữ liệu cập nhật
     * @return DTO thể loại sau khi cập nhật
     */
    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequestForm form) {
        CategoryResponseDto updatedCategory = categoryService.updateCategory(id, form);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thể loại thành công!", updatedCategory));
    }

    /**
     * API xóa thể loại sách khỏi hệ thống (Yêu cầu quyền ADMIN).
     *
     * @param id ID của thể loại cần xóa
     * @return Phản hồi thông báo xóa thành công
     */
    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa thể loại thành công!", null));
    }

    /**
     * API công khai lấy danh sách toàn bộ các thể loại sách (Không cần đăng nhập, tiền tố /public).
     *
     * @return Danh sách thể loại
     */
    @GetMapping("/public/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getAllCategories() {
        List<CategoryResponseDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thể loại thành công!", categories));
    }

    /**
     * API công khai lấy danh sách các thể loại đang hiển thị công khai (status = UNHIDE) cho khách hàng.
     * Trả về danh sách rút gọn chỉ gồm categoryId và categoryName, phục vụ menu dropdown ngang.
     *
     * @return Danh sách CategorySimpleDto bọc trong ApiResponse
     */
    @GetMapping("/public/categories/active")
    public ResponseEntity<ApiResponse<List<CategorySimpleDto>>> getActiveCategories() {
        List<CategorySimpleDto> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thể loại hiển thị thành công!", categories));
    }

    /**
     * API công khai lấy thông tin chi tiết một thể loại theo ID (Không cần đăng nhập, tiền tố /public).
     *
     * @param id ID của thể loại
     * @return DTO chi tiết thể loại
     */
    @GetMapping("/public/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryById(@PathVariable Long id) {
        CategoryResponseDto category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin thể loại thành công!", category));
    }

    /**
     * API lấy danh sách phân trang các thể loại kèm bộ lọc tìm kiếm và sắp xếp (Dành riêng cho ADMIN).
     * Mặc định sắp xếp theo createdAt với thời gian mới nhất lên đầu (DESC).
     *
     * @param filter Bộ lọc tìm kiếm và tham số phân trang nhận qua Query Parameters
     * @return Trang kết quả chứa danh sách DTO thể loại bọc trong ApiResponse với HTTP 200 OK
     */
    @GetMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<CategoryResponseDto>>> getCategoriesPage(
            @Valid @ModelAttribute CategoryFilterRequestForm filter) {
        // Bước 1: Gọi tầng Service xử lý truy vấn phân trang qua Specification
        Page<CategoryResponseDto> result = categoryService.getCategoriesWithFilter(filter);

        // Bước 2: Bọc kết quả trả về trong chuẩn ApiResponse với HTTP 200 OK
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thể loại phân trang thành công!", result));
    }

    /**
     * API công khai lấy danh sách toàn bộ các giá trị trạng thái hiển thị của thể loại (Không cần đăng nhập, tiền tố /public).
     *
     * @return Danh sách các enum DisplayStatus (HIDE, UNHIDE) bọc trong chuẩn ApiResponse
     */
    @GetMapping("/public/categories/statuses")
    public ResponseEntity<ApiResponse<List<DisplayStatus>>> getCategoryStatuses() {
        // Bước 1: Gọi tầng Service lấy danh sách các trạng thái hiển thị
        List<DisplayStatus> statuses = categoryService.getDisplayStatuses();

        // Bước 2: Bọc kết quả trả về trong chuẩn ApiResponse với HTTP 200 OK
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách trạng thái thể loại thành công!", statuses));
    }
}
