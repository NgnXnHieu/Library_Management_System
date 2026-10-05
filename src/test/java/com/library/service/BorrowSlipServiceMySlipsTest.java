package com.library.service;

import com.library.dto.borrow.BorrowSlipResponseDto;
import com.library.entity.Account;
import com.library.entity.BorrowSlip;
import com.library.entity.User;
import com.library.enums.AccountStatus;
import com.library.enums.BorrowStatus;
import com.library.enums.PaymentStatus;
import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import com.library.mapper.BorrowSlipMapper;
import com.library.repository.AccountRepository;
import com.library.repository.BorrowItemRepository;
import com.library.repository.BorrowSlipRepository;
import com.library.repository.UserRepository;
import com.library.requestform.borrow.BorrowSlipFilterRequestForm;
import com.library.security.UserDetailCustom;
import com.library.service.impl.BorrowSlipServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Test kiểm thử logic nghiệp vụ getMyBorrowSlips trong BorrowSlipServiceImpl:
 * - Kiểm tra thông tin tài khoản và người dùng từ SecurityContextHolder
 * - Kiểm tra trạng thái LOCKED / INACTIVE
 * - Lọc danh sách theo userId của người dùng hiện tại
 */
@ExtendWith(MockitoExtension.class)
class BorrowSlipServiceMySlipsTest {

    @Mock
    private BorrowSlipRepository borrowSlipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BorrowItemRepository borrowItemRepository;

    @Mock
    private BorrowSlipMapper borrowSlipMapper;

    @InjectMocks
    private BorrowSlipServiceImpl borrowSlipService;

    private UserDetailCustom userDetails;
    private Account activeAccount;
    private User activeUser;

    @BeforeEach
    void setUp() {
        userDetails = UserDetailCustom.builder()
                .accountId(10L)
                .userId(100L)
                .branchId(null)
                .roleCode("CUSTOMER")
                .username("customer01")
                .fullName("Khách hàng 01")
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        activeAccount = Account.builder()
                .username("customer01")
                .passwordHash("hashed")
                .status("ACTIVE")
                .build();
        activeAccount.setId(10L);

        activeUser = User.builder()
                .email("customer01@example.com")
                .fullName("Khách hàng 01")
                .status(AccountStatus.ACTIVE)
                .build();
        activeUser.setId(100L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Test 1: Lấy danh sách phiếu mượn cá nhân thành công khi tài khoản và người dùng ACTIVE")
    void testGetMyBorrowSlipsSuccess() {
        // Arrange
        when(accountRepository.findById(10L)).thenReturn(Optional.of(activeAccount));
        when(userRepository.findById(100L)).thenReturn(Optional.of(activeUser));

        BorrowSlipResponseDto dto = BorrowSlipResponseDto.builder()
                .id(1L)
                .borrowCode("BS-001")
                .customerId(100L)
                .build();

        Page<BorrowSlipResponseDto> dtoPage = new PageImpl<>(List.of(dto));
        when(borrowSlipRepository.findAllBorrowSlipsWithFilter(any(BorrowSlipFilterRequestForm.class), any(Pageable.class)))
                .thenReturn(dtoPage);
        when(borrowItemRepository.findAllByBorrowSlipIdInWithBook(List.of(1L))).thenReturn(Collections.emptyList());

        BorrowSlipFilterRequestForm filter = new BorrowSlipFilterRequestForm();

        // Act
        Page<BorrowSlipResponseDto> result = borrowSlipService.getMyBorrowSlips(filter);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(100L, filter.getCustomerId());
        verify(accountRepository).findById(10L);
        verify(userRepository).findById(100L);
    }

    @Test
    @DisplayName("Test 2: Thất bại khi Account không tồn tại trong DB")
    void testGetMyBorrowSlipsAccountNotFound() {
        // Arrange
        when(accountRepository.findById(10L)).thenReturn(Optional.empty());

        BorrowSlipFilterRequestForm filter = new BorrowSlipFilterRequestForm();

        // Act & Assert
        AppException ex = assertThrows(AppException.class, () -> borrowSlipService.getMyBorrowSlips(filter));
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("Test 3: Thất bại khi Account đang bị LOCKED")
    void testGetMyBorrowSlipsAccountLocked() {
        // Arrange
        Account lockedAccount = Account.builder()
                .username("customer01")
                .status("LOCKED")
                .build();
        lockedAccount.setId(10L);
        when(accountRepository.findById(10L)).thenReturn(Optional.of(lockedAccount));

        BorrowSlipFilterRequestForm filter = new BorrowSlipFilterRequestForm();

        // Act & Assert
        AppException ex = assertThrows(AppException.class, () -> borrowSlipService.getMyBorrowSlips(filter));
        assertEquals(ErrorCode.ACCOUNT_INACTIVE_OR_LOCKED, ex.getErrorCode());
    }

    @Test
    @DisplayName("Test 4: Thất bại khi User đang bị INACTIVE")
    void testGetMyBorrowSlipsUserInactive() {
        // Arrange
        when(accountRepository.findById(10L)).thenReturn(Optional.of(activeAccount));

        User inactiveUser = User.builder()
                .status(AccountStatus.INACTIVE)
                .build();
        inactiveUser.setId(100L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(inactiveUser));

        BorrowSlipFilterRequestForm filter = new BorrowSlipFilterRequestForm();

        // Act & Assert
        AppException ex = assertThrows(AppException.class, () -> borrowSlipService.getMyBorrowSlips(filter));
        assertEquals(ErrorCode.ACCOUNT_INACTIVE_OR_LOCKED, ex.getErrorCode());
    }
}
