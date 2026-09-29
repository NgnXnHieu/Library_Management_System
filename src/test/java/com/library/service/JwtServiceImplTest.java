package com.library.service;

import com.library.entity.Account;
import com.library.entity.Branch;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.enums.AccountStatus;
import com.library.exception.BadRequestException;
import com.library.repository.AccountRepository;
import com.library.repository.UserRepository;
import com.library.security.UserDetailCustom;
import com.library.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceImplTest {

    private JwtServiceImpl jwtService;
    private AccountRepository accountRepository;
    private UserRepository userRepository;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long accessExpiration = 60000; // 1 minute
    private final long refreshExpiration = 120000; // 2 minutes

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        userRepository = mock(UserRepository.class);
        jwtService = new JwtServiceImpl(secret, accessExpiration, refreshExpiration, accountRepository, userRepository);
    }

    @Test
    void testGenerateAndExtractAccessToken() {
        Long accountId = 123L;

        String token = jwtService.generateAccessToken(accountId);
        assertNotNull(token);

        assertEquals(accountId, jwtService.extractAccountId(token));
        assertEquals("ACCESS", jwtService.extractTokenType(token));
        assertTrue(jwtService.isAccessToken(token));
        assertFalse(jwtService.isRefreshToken(token));
        assertTrue(jwtService.isTokenValid(token));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void testGenerateAndExtractRefreshToken() {
        Long accountId = 456L;

        String token = jwtService.generateRefreshToken(accountId);
        assertNotNull(token);

        assertEquals(accountId, jwtService.extractAccountId(token));
        assertEquals("REFRESH", jwtService.extractTokenType(token));
        assertTrue(jwtService.isRefreshToken(token));
        assertFalse(jwtService.isAccessToken(token));
        assertTrue(jwtService.isTokenValid(token));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid.token.here"));
        assertFalse(jwtService.isAccessToken("invalid.token.here"));
    }

    @Test
    void testExpiredToken() {
        JwtServiceImpl shortLivedJwtService = new JwtServiceImpl(secret, -5000, -5000);
        String expiredToken = shortLivedJwtService.generateAccessToken(123L);

        assertFalse(jwtService.isTokenValid(expiredToken));
        assertTrue(jwtService.isTokenExpired(expiredToken));
    }

    @Test
    void testGetUserDetailsSuccessWithBranchAndRole() {
        Long accountId = 1L;
        Account account = Account.builder()
                .id(accountId)
                .username("test_user_01")
                .status("ACTIVE")
                .build();

        Role role = Role.builder()
                .id(10L)
                .code("LIBRARIAN")
                .name("Thủ thư")
                .build();

        Branch branch = Branch.builder()
                .id(100L)
                .code("BR01")
                .name("Chi nhánh Hà Nội")
                .build();

        User user = User.builder()
                .id(20L)
                .fullName("Nguyễn Văn A")
                .status(AccountStatus.ACTIVE)
                .role(role)
                .branch(branch)
                .account(account)
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(userRepository.findByAccountIdWithDetails(accountId)).thenReturn(Optional.of(user));

        UserDetailCustom details = jwtService.getUserDetailsByAccountId(accountId);

        assertNotNull(details);
        assertEquals(accountId, details.getAccountId());
        assertEquals(20L, details.getUserId());
        assertEquals(100L, details.getBranchId());
        assertEquals("LIBRARIAN", details.getRoleCode());
        assertEquals("test_user_01", details.getUsername());
        assertEquals("Nguyễn Văn A", details.getFullName());
    }

    @Test
    void testGetUserDetailsSuccessWithoutBranch() {
        Long accountId = 2L;
        Account account = Account.builder()
                .id(accountId)
                .username("reader_user")
                .status("ACTIVE")
                .build();

        Role role = Role.builder()
                .id(11L)
                .code("READER")
                .name("Độc giả")
                .build();

        User user = User.builder()
                .id(21L)
                .fullName("Trần Thị B")
                .status(AccountStatus.ACTIVE)
                .role(role)
                .branch(null) // Không có chi nhánh
                .account(account)
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(userRepository.findByAccountIdWithDetails(accountId)).thenReturn(Optional.of(user));

        UserDetailCustom details = jwtService.getUserDetailsByAccountId(accountId);

        assertNotNull(details);
        assertEquals(accountId, details.getAccountId());
        assertEquals(21L, details.getUserId());
        assertNull(details.getBranchId());
        assertEquals("READER", details.getRoleCode());
    }

    @Test
    void testGetUserDetailsNullAccountIdThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> jwtService.getUserDetailsByAccountId(null));
    }

    @Test
    void testGetUserDetailsAccountNotFoundThrowsBadRequest() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> jwtService.getUserDetailsByAccountId(999L));
    }

    @Test
    void testGetUserDetailsAccountInactiveThrowsBadRequest() {
        Account inactiveAccount = Account.builder()
                .id(3L)
                .username("inactive_user")
                .status("LOCKED")
                .build();

        when(accountRepository.findById(3L)).thenReturn(Optional.of(inactiveAccount));

        assertThrows(BadRequestException.class, () -> jwtService.getUserDetailsByAccountId(3L));
    }

    @Test
    void testGetUserDetailsUserNotFoundThrowsBadRequest() {
        Account account = Account.builder()
                .id(4L)
                .username("orphan_account")
                .status("ACTIVE")
                .build();

        when(accountRepository.findById(4L)).thenReturn(Optional.of(account));
        when(userRepository.findByAccountIdWithDetails(4L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> jwtService.getUserDetailsByAccountId(4L));
    }

    @Test
    void testGetUserDetailsUserInactiveThrowsBadRequest() {
        Account account = Account.builder()
                .id(5L)
                .username("inactive_user_profile")
                .status("ACTIVE")
                .build();

        User inactiveUser = User.builder()
                .id(25L)
                .fullName("Lê Văn C")
                .status(AccountStatus.LOCKED)
                .build();

        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
        when(userRepository.findByAccountIdWithDetails(5L)).thenReturn(Optional.of(inactiveUser));

        assertThrows(BadRequestException.class, () -> jwtService.getUserDetailsByAccountId(5L));
    }
}

