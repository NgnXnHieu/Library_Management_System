package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.book.BookResponseDto;
import com.library.requestform.book.BookCreateRequestForm;
import com.library.requestform.book.BookFilterRequestForm;
import com.library.requestform.book.BookUpdateRequestForm;
import com.library.service.BookService;
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

/**
 * Controller tiếp nhận và xử lý các yêu cầu liên quan đến đầu sách (Book).
 */
@RestController
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * API thêm mới một đầu sách vào hệ thống (Chỉ dành cho ADMIN).
     *
     * @param form Dữ liệu tạo sách: categoryId, isbn, title, author, publisher, publicationYear, description, coverImageKey, price, rentalPrice, status
     * @return DTO đầu sách vừa tạo bọc trong chuẩn ApiResponse với HTTP 201
     */
    @PostMapping("/books")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookResponseDto>> createBook(
            @Valid @RequestBody BookCreateRequestForm form) {
        BookResponseDto createdBook = bookService.createBook(form);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm mới đầu sách thành công!", createdBook));
    }

    /**
     * API công khai lấy danh sách toàn bộ các đầu sách kèm bộ lọc, sắp xếp và phân trang.
     * Sử dụng JOIN FETCH Category để tối ưu hóa truy vấn và tránh N+1 query.
     *
     * @param filter Bộ lọc tìm kiếm và phân trang nhận qua Query Parameters
     * @return Danh sách sách phân trang bọc trong ApiResponse
     */
    @GetMapping("/public/books")
    public ResponseEntity<ApiResponse<Page<BookResponseDto>>> getAllBooks(
            @ModelAttribute BookFilterRequestForm filter) {
        Page<BookResponseDto> books = bookService.getAllBooks(filter);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đầu sách thành công!", books));
    }

    /**
     * API lấy danh sách phân trang các đầu sách với bộ lọc đầy đủ (Chỉ dành cho ADMIN).
     *
     * @param filter Bộ lọc tìm kiếm và phân trang
     * @return Danh sách đầu sách phân trang bọc trong ApiResponse
     */
    @GetMapping("/admin/books")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<BookResponseDto>>> getAdminBooks(
            @ModelAttribute BookFilterRequestForm filter) {
        Page<BookResponseDto> books = bookService.getBooksWithFilter(filter);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đầu sách cho quản trị viên thành công!", books));
    }

    /**
     * API cập nhật thông tin đầu sách theo ID (Chỉ dành cho ADMIN).
     * Cho phép cập nhật từng phần (partial update) - các trường không truyền (null) sẽ được giữ nguyên.
     *
     * @param id   ID của đầu sách cần cập nhật
     * @param form Dữ liệu cập nhật sách nhận từ Request Body
     * @return DTO đầu sách sau khi cập nhật bọc trong chuẩn ApiResponse
     */
    @PutMapping("/books/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookResponseDto>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequestForm form) {
        BookResponseDto updatedBook = bookService.updateBook(id, form);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin sách thành công!", updatedBook));
    }

    /**
     * API xóa đầu sách khỏi hệ thống (Chỉ dành cho ADMIN).
     *
     * @param id ID của đầu sách cần xóa
     * @return Thông báo kết quả bọc trong ApiResponse
     */
    @DeleteMapping("/books/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa đầu sách thành công!", null));
    }
}
