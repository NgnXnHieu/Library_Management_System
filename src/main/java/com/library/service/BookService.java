package com.library.service;

import com.library.dto.book.BookResponseDto;
import com.library.requestform.book.BookCreateRequestForm;
import com.library.requestform.book.BookFilterRequestForm;
import com.library.requestform.book.BookUpdateRequestForm;
import org.springframework.data.domain.Page;

/**
 * Interface định nghĩa các nghiệp vụ quản lý đầu sách.
 */
public interface BookService {

    /**
     * Thêm mới một đầu sách vào hệ thống (Chỉ dành cho ADMIN).
     *
     * @param form Dữ liệu tạo mới sách
     * @return DTO thông tin sách vừa tạo
     */
    BookResponseDto createBook(BookCreateRequestForm form);

    /**
     * Lấy danh sách toàn bộ các đầu sách kèm bộ lọc, sắp xếp và phân trang (Public).
     * Sử dụng JOIN FETCH Category để tối ưu hiệu năng.
     *
     * @param filter Bộ lọc tìm kiếm và phân trang
     * @return Trang kết quả chứa danh sách BookResponseDto
     */
    Page<BookResponseDto> getAllBooks(BookFilterRequestForm filter);

    /**
     * Cập nhật thông tin đầu sách theo ID (Chỉ dành cho ADMIN).
     * Các trường null trong form sẽ được giữ nguyên dữ liệu hiện tại.
     *
     * @param id   ID của đầu sách cần cập nhật
     * @param form Dữ liệu cập nhật sách
     * @return DTO thông tin sách sau khi cập nhật
     */
    BookResponseDto updateBook(Long id, BookUpdateRequestForm form);

    /**
     * Lấy danh sách phân trang các đầu sách kèm theo bộ lọc mở rộng và sắp xếp (Dành riêng cho ADMIN).
     *
     * @param filter Bộ lọc tìm kiếm và thông tin phân trang
     * @return Trang kết quả chứa danh sách BookResponseDto
     */
    Page<BookResponseDto> getBooksWithFilter(BookFilterRequestForm filter);

    /**
     * Xóa đầu sách khỏi hệ thống (Chỉ dành cho ADMIN).
     *
     * @param id ID của đầu sách cần xóa
     */
    void deleteBook(Long id);
}
