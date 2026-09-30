package com.library.service;

import com.library.dto.user.UserResponseDto;
import com.library.entity.Account;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.enums.AccountStatus;
import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import com.library.mapper.AccountMapper;
import com.library.mapper.UserMapper;
import com.library.repository.AccountRepository;
import com.library.repository.BranchRepository;
import com.library.repository.RoleRepository;
import com.library.repository.UserRepository;
import com.library.requestform.account.RegisterRequestForm;
import com.library.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    private RegisterRequestForm registerForm;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        registerForm = RegisterRequestForm.builder()
                .username("customer_user1")
                .password("Password@123")
                .email("customer@example.com")
                .phone("0987654321")
                .fullName("Nguyen Van A")
                .build();

        customerRole = Role.builder()
                .code("CUSTOMER")
                .name("Khách hàng")
                .description("Vai trò khách hàng")
                .build();
        customerRole.setId(3L);
    }

    @Test
    @DisplayName("Ném lỗi ACCOUNT_ALREADY_EXISTS khi tên tài khoản đã tồn tại")
    void testCreateCustomerAccount_UsernameAlreadyExists_ThrowsException() {
        when(accountRepository.existsByUsername("customer_user1")).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () ->
                accountService.createCustomerAccount(registerForm));

        assertEquals(ErrorCode.ACCOUNT_ALREADY_EXISTS, exception.getErrorCode());
        verify(userRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ném lỗi EMAIL_ALREADY_EXISTS khi email đã tồn tại")
    void testCreateCustomerAccount_EmailAlreadyExists_ThrowsException() {
        when(accountRepository.existsByUsername("customer_user1")).thenReturn(false);
        when(userRepository.existsByEmail("customer@example.com")).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () ->
                accountService.createCustomerAccount(registerForm));

        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, exception.getErrorCode());
        verify(userRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ném lỗi PHONE_ALREADY_EXISTS khi số điện thoại đã tồn tại")
    void testCreateCustomerAccount_PhoneAlreadyExists_ThrowsException() {
        when(accountRepository.existsByUsername("customer_user1")).thenReturn(false);
        when(userRepository.existsByEmail("customer@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("0987654321")).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () ->
                accountService.createCustomerAccount(registerForm));

        assertEquals(ErrorCode.PHONE_ALREADY_EXISTS, exception.getErrorCode());
        verify(userRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tạo tài khoản khách hàng thành công với branch=null, role=CUSTOMER, password được mã hóa")
    void testCreateCustomerAccount_Success() {
        // Arrange
        when(accountRepository.existsByUsername("customer_user1")).thenReturn(false);
        when(userRepository.existsByEmail("customer@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("0987654321")).thenReturn(false);
        when(roleRepository.findByCode("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword123");

        Account savedAccount = Account.builder()
                .username("customer_user1")
                .passwordHash("encodedPassword123")
                .status("ACTIVE")
                .build();
        savedAccount.setId(10L);
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        User savedUser = User.builder()
                .account(savedAccount)
                .role(customerRole)
                .branch(null)
                .fullName("Nguyen Van A")
                .email("customer@example.com")
                .phone("0987654321")
                .status(AccountStatus.ACTIVE)
                .build();
        savedUser.setId(20L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDto expectedDto = UserResponseDto.builder()
                .userId(20L)
                .accountId(10L)
                .username("customer_user1")
                .role("CUSTOMER")
                .branchId(null)
                .branchName(null)
                .fullName("Nguyen Van A")
                .email("customer@example.com")
                .phone("0987654321")
                .status("ACTIVE")
                .build();
        when(userMapper.toDto(savedUser)).thenReturn(expectedDto);

        // Act
        UserResponseDto result = accountService.createCustomerAccount(registerForm);

        // Assert
        assertNotNull(result);
        assertEquals("customer_user1", result.getUsername());
        assertEquals("CUSTOMER", result.getRole());
        assertNull(result.getBranchId());

        // Kiểm tra đối tượng Account được lưu
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        Account capturedAccount = accountCaptor.getValue();
        assertEquals("customer_user1", capturedAccount.getUsername());
        assertEquals("encodedPassword123", capturedAccount.getPasswordHash());
        assertEquals("ACTIVE", capturedAccount.getStatus());

        // Kiểm tra đối tượng User được lưu
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals(savedAccount, capturedUser.getAccount());
        assertEquals("CUSTOMER", capturedUser.getRole().getCode());
        assertNull(capturedUser.getBranch());
        assertEquals("Nguyen Van A", capturedUser.getFullName());
        assertEquals("customer@example.com", capturedUser.getEmail());
        assertEquals("0987654321", capturedUser.getPhone());
        assertEquals(AccountStatus.ACTIVE, capturedUser.getStatus());
    }
}
