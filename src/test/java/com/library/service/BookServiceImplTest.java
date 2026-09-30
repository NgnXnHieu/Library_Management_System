package com.library.service;

import com.library.dto.book.BookResponseDto;
import com.library.entity.Book;
import com.library.entity.Category;
import com.library.enums.DisplayStatus;
import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import com.library.mapper.BookMapper;
import com.library.repository.BookRepository;
import com.library.repository.CategoryRepository;
import com.library.requestform.book.BookUpdateRequestForm;
import com.library.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book existingBook;
    private Category existingCategory;

    @BeforeEach
    void setUp() {
        existingCategory = Category.builder()
                .name("Công nghệ thông tin")
                .build();
        existingCategory.setId(1L);

        existingBook = Book.builder()
                .category(existingCategory)
                .isbn("978-604-0-12345-6")
                .title("Lập trình Java căn bản")
                .author("Nguyễn Văn A")
                .publisher("NXB Giáo Dục")
                .publicationYear(2023)
                .description("Sách học Java")
                .coverImageKey("java_cover.jpg")
                .price(new BigDecimal("150000.00"))
                .rentalPrice(new BigDecimal("15000.00"))
                .fineAmount(new BigDecimal("5000.00"))
                .status(DisplayStatus.UNHIDE)
                .build();
        existingBook.setId(10L);
    }

    @Test
    @DisplayName("Cập nhật sách thành công khi chỉ truyền một vài trường (partial update)")
    void testUpdateBook_PartialUpdate_Success() {
        BookUpdateRequestForm form = BookUpdateRequestForm.builder()
                .title("   Lập trình Java nâng cao   ")
                .price(new BigDecimal("200000.00"))
                .build();

        when(bookRepository.findById(10L)).thenReturn(Optional.of(existingBook));
        doAnswer(invocation -> {
            Book b = invocation.getArgument(1);
            b.setPrice(new BigDecimal("200000.00"));
            return null;
        }).when(bookMapper).updateEntityFromForm(eq(form), any(Book.class));

        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookResponseDto expectedDto = BookResponseDto.builder()
                .id(10L)
                .title("Lập trình Java nâng cao")
                .price(new BigDecimal("200000.00"))
                .author("Nguyễn Văn A")
                .build();
        when(bookMapper.toDto(any(Book.class))).thenReturn(expectedDto);

        BookResponseDto result = bookService.updateBook(10L, form);

        assertNotNull(result);
        assertEquals("Lập trình Java nâng cao", existingBook.getTitle());
        assertEquals(new BigDecimal("200000.00"), existingBook.getPrice());
        assertEquals("Nguyễn Văn A", existingBook.getAuthor()); // Giữ nguyên
        verify(bookRepository).save(existingBook);
    }

    @Test
    @DisplayName("Ném lỗi BOOK_NOT_FOUND khi cập nhật sách với ID không tồn tại")
    void testUpdateBook_NotFound_ThrowsException() {
        BookUpdateRequestForm form = BookUpdateRequestForm.builder().title("Tên mới").build();
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> bookService.updateBook(999L, form));

        assertEquals(ErrorCode.BOOK_NOT_FOUND, exception.getErrorCode());
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ném lỗi CATEGORY_NOT_FOUND khi cập nhật thể loại không tồn tại")
    void testUpdateBook_CategoryNotFound_ThrowsException() {
        BookUpdateRequestForm form = BookUpdateRequestForm.builder().categoryId(99L).build();
        when(bookRepository.findById(10L)).thenReturn(Optional.of(existingBook));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> bookService.updateBook(10L, form));

        assertEquals(ErrorCode.CATEGORY_NOT_FOUND, exception.getErrorCode());
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ném lỗi BOOK_ALREADY_EXISTS khi đổi ISBN sang mã đã thuộc về cuốn sách khác")
    void testUpdateBook_DuplicateIsbn_ThrowsException() {
        BookUpdateRequestForm form = BookUpdateRequestForm.builder().isbn("978-999-9-99999-9").build();
        when(bookRepository.findById(10L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.existsByIsbnAndIdNot("978-999-9-99999-9", 10L)).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> bookService.updateBook(10L, form));

        assertEquals(ErrorCode.BOOK_ALREADY_EXISTS, exception.getErrorCode());
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cập nhật thành công khi đổi thể loại hợp lệ")
    void testUpdateBook_ChangeCategory_Success() {
        Category newCategory = Category.builder().name("Kỹ năng sống").build();
        newCategory.setId(2L);

        BookUpdateRequestForm form = BookUpdateRequestForm.builder().categoryId(2L).build();
        when(bookRepository.findById(10L)).thenReturn(Optional.of(existingBook));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookResponseDto expectedDto = BookResponseDto.builder().id(10L).categoryId(2L).build();
        when(bookMapper.toDto(any(Book.class))).thenReturn(expectedDto);

        BookResponseDto result = bookService.updateBook(10L, form);

        assertNotNull(result);
        assertEquals(2L, existingBook.getCategory().getId());
        verify(bookRepository).save(existingBook);
    }
}
