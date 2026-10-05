package com.library.controller;

import com.library.dto.borrow.BorrowItemResponseDto;
import com.library.dto.borrow.BorrowSlipResponseDto;
import com.library.enums.BorrowStatus;
import com.library.enums.PaymentStatus;
import com.library.requestform.borrow.BorrowSlipFilterRequestForm;
import com.library.security.JwtAuthenticationFilter;
import com.library.service.BorrowSlipService;
import com.library.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit Test kiểm thử API lấy danh sách chi tiết phiếu mượn cho tài khoản đang đăng nhập:
 * GET /borrow-slips/my-slips
 */
@WebMvcTest(BorrowSlipController.class)
@AutoConfigureMockMvc(addFilters = false)
class BorrowSlipCustomerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BorrowSlipService borrowSlipService;

    @MockBean
    private JwtServiceImpl jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("Test 1: Lấy danh sách phiếu mượn cá nhân thành công với đầy đủ chi tiết staff, branch, items")
    void testGetMyBorrowSlipsSuccess() throws Exception {
        // Arrange
        BorrowItemResponseDto item1 = BorrowItemResponseDto.builder()
                .id(1L)
                .inventoryId(10L)
                .bookId(100L)
                .bookTitle("Clean Architecture")
                .isbn("978-0134494166")
                .rentalPrice(new BigDecimal("15000"))
                .quantity(1)
                .build();

        BorrowSlipResponseDto slip1 = BorrowSlipResponseDto.builder()
                .id(1L)
                .borrowCode("BS-12345")
                .customerId(1001L)
                .customerName("Nguyễn Văn A")
                .customerPhone("0912345678")
                .staffId(2001L)
                .staffName("Thủ Thư B")
                .staffPhone("0987654321")
                .branchId(5L)
                .branchName("Chi nhánh Cầu Giấy")
                .borrowedAt(LocalDateTime.of(2026, 10, 1, 9, 0))
                .dueAt(LocalDateTime.of(2026, 10, 15, 17, 0))
                .status(BorrowStatus.BORROWED)
                .paymentStatus(PaymentStatus.PAID)
                .totalQuantity(1)
                .totalAmount(new BigDecimal("15000"))
                .items(List.of(item1))
                .build();

        Page<BorrowSlipResponseDto> pageResult = new PageImpl<>(List.of(slip1), PageRequest.of(0, 10), 1);

        when(borrowSlipService.getMyBorrowSlips(any(BorrowSlipFilterRequestForm.class)))
                .thenReturn(pageResult);

        // Act & Assert
        mockMvc.perform(get("/borrow-slips/my-slips")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Lấy danh sách phiếu mượn cá nhân thành công!"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].borrowCode").value("BS-12345"))
                .andExpect(jsonPath("$.data.content[0].branchId").value(5))
                .andExpect(jsonPath("$.data.content[0].branchName").value("Chi nhánh Cầu Giấy"))
                .andExpect(jsonPath("$.data.content[0].staffId").value(2001))
                .andExpect(jsonPath("$.data.content[0].staffName").value("Thủ Thư B"))
                .andExpect(jsonPath("$.data.content[0].staffPhone").value("0987654321"))
                .andExpect(jsonPath("$.data.content[0].status").value("BORROWED"))
                .andExpect(jsonPath("$.data.content[0].paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.data.content[0].items[0].bookTitle").value("Clean Architecture"))
                .andExpect(jsonPath("$.data.content[0].items[0].isbn").value("978-0134494166"));
    }

    @Test
    @DisplayName("Test 2: Lọc theo trạng thái mượn và trạng thái thanh toán")
    void testGetMyBorrowSlipsWithFilters() throws Exception {
        // Arrange
        Page<BorrowSlipResponseDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(borrowSlipService.getMyBorrowSlips(any(BorrowSlipFilterRequestForm.class)))
                .thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/borrow-slips/my-slips")
                        .param("status", "BORROWED")
                        .param("paymentStatus", "UNPAID")
                        .param("sortBy", "borrowedAt")
                        .param("sortDir", "desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(0));

        ArgumentCaptor<BorrowSlipFilterRequestForm> formCaptor = ArgumentCaptor.forClass(BorrowSlipFilterRequestForm.class);
        verify(borrowSlipService).getMyBorrowSlips(formCaptor.capture());

        BorrowSlipFilterRequestForm captured = formCaptor.getValue();
        assertEquals(BorrowStatus.BORROWED, captured.getStatus());
        assertEquals(PaymentStatus.UNPAID, captured.getPaymentStatus());
        assertEquals("borrowedAt", captured.getSortBy());
        assertEquals("desc", captured.getSortDir());
    }
}
