package com.library.controller;

import com.library.dto.book.BookBranchInventoryDto;
import com.library.dto.book.BookDetailCustomerResponseDto;
import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import com.library.security.JwtAuthenticationFilter;
import com.library.service.BookService;
import com.library.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test kiểm thử API công khai xem thông tin chi tiết sách dành cho khách hàng:
 * GET /public/books/{bookId}
 */
@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerCustomerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private JwtServiceImpl jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("Test 1: Lấy chi tiết sách thành công khi không truyền branchId")
    void testGetBookDetailSuccessWithoutBranchId() throws Exception {
        Long bookId = 1L;

        BookBranchInventoryDto inv1 = BookBranchInventoryDto.builder()
                .inventoryId(101L)
                .branchId(1L)
                .branchName("Chi nhánh Hà Nội")
                .branchAddress("123 Cầu Giấy")
                .branchPhone("0987654321")
                .totalQuantity(20)
                .availableQuantity(15)
                .shelfLocation("Kệ A1")
                .status("UNHIDE")
                .build();

        BookBranchInventoryDto inv2 = BookBranchInventoryDto.builder()
                .inventoryId(102L)
                .branchId(2L)
                .branchName("Chi nhánh TP.HCM")
                .branchAddress("456 Quận 1")
                .branchPhone("0123456789")
                .totalQuantity(10)
                .availableQuantity(8)
                .shelfLocation("Kệ B2")
                .status("UNHIDE")
                .build();

        BookDetailCustomerResponseDto responseDto = BookDetailCustomerResponseDto.builder()
                .id(bookId)
                .isbn("978-604-0-12345-6")
                .title("Lập Trình Java Nâng Cao")
                .author("Nguyễn Văn A")
                .publisher("NXB Giáo Dục")
                .publicationYear(2024)
                .description("Sách hướng dẫn Java chuyên sâu")
                .coverImageKey("books/java.jpg")
                .coverImageUrl("http://localhost:8080/uploads/books/java.jpg")
                .price(BigDecimal.valueOf(150000))
                .rentalPrice(BigDecimal.valueOf(15000))
                .fineAmount(BigDecimal.valueOf(5000))
                .status("UNHIDE")
                .categoryId(1L)
                .categoryName("Công nghệ thông tin")
                .inventories(List.of(inv1, inv2))
                .build();

        when(bookService.getBookDetailForCustomer(bookId, null)).thenReturn(responseDto);

        mockMvc.perform(get("/public/books/{bookId}", bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lấy thông tin chi tiết đầu sách thành công!"))
                .andExpect(jsonPath("$.data.id").value(bookId))
                .andExpect(jsonPath("$.data.title").value("Lập Trình Java Nâng Cao"))
                .andExpect(jsonPath("$.data.categoryName").value("Công nghệ thông tin"))
                .andExpect(jsonPath("$.data.inventories.length()").value(2))
                .andExpect(jsonPath("$.data.inventories[0].branchName").value("Chi nhánh Hà Nội"))
                .andExpect(jsonPath("$.data.inventories[1].branchName").value("Chi nhánh TP.HCM"));
    }

    @Test
    @DisplayName("Test 2: Lấy chi tiết sách thành công khi có truyền branchId")
    void testGetBookDetailSuccessWithBranchId() throws Exception {
        Long bookId = 1L;
        Long branchId = 2L;

        BookBranchInventoryDto inv = BookBranchInventoryDto.builder()
                .inventoryId(102L)
                .branchId(branchId)
                .branchName("Chi nhánh TP.HCM")
                .branchAddress("456 Quận 1")
                .branchPhone("0123456789")
                .totalQuantity(10)
                .availableQuantity(8)
                .shelfLocation("Kệ B2")
                .status("UNHIDE")
                .build();

        BookDetailCustomerResponseDto responseDto = BookDetailCustomerResponseDto.builder()
                .id(bookId)
                .isbn("978-604-0-12345-6")
                .title("Lập Trình Java Nâng Cao")
                .inventories(List.of(inv))
                .build();

        when(bookService.getBookDetailForCustomer(bookId, branchId)).thenReturn(responseDto);

        mockMvc.perform(get("/public/books/{bookId}", bookId)
                        .param("branchId", String.valueOf(branchId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(bookId))
                .andExpect(jsonPath("$.data.inventories.length()").value(1))
                .andExpect(jsonPath("$.data.inventories[0].branchId").value(branchId))
                .andExpect(jsonPath("$.data.inventories[0].branchName").value("Chi nhánh TP.HCM"));
    }

    @Test
    @DisplayName("Test 3: Báo lỗi BOOK_NOT_FOUND khi sách không tồn tại")
    void testGetBookDetailNotFound() throws Exception {
        Long bookId = 999L;

        when(bookService.getBookDetailForCustomer(bookId, null))
                .thenThrow(new AppException(ErrorCode.BOOK_NOT_FOUND, bookId));

        mockMvc.perform(get("/public/books/{bookId}", bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("BOOK_NOT_FOUND"));
    }

    @Test
    @DisplayName("Test 4: Báo lỗi BOOK_HIDDEN khi sách đang ở trạng thái ẩn")
    void testGetBookDetailHidden() throws Exception {
        Long bookId = 2L;

        when(bookService.getBookDetailForCustomer(bookId, null))
                .thenThrow(new AppException(ErrorCode.BOOK_HIDDEN, "Sách Tạm Ẩn"));

        mockMvc.perform(get("/public/books/{bookId}", bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BOOK_HIDDEN"));
    }

    @Test
    @DisplayName("Test 5: Báo lỗi BRANCH_CLOSED khi chi nhánh đang đóng cửa")
    void testGetBookDetailBranchClosed() throws Exception {
        Long bookId = 1L;
        Long branchId = 3L;

        when(bookService.getBookDetailForCustomer(bookId, branchId))
                .thenThrow(new AppException(ErrorCode.BRANCH_CLOSED, "Chi Nhánh Đóng Cửa"));

        mockMvc.perform(get("/public/books/{bookId}", bookId)
                        .param("branchId", String.valueOf(branchId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BRANCH_CLOSED"));
    }
}
