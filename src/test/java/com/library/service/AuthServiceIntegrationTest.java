package com.library.service;

import com.library.dto.auth.LoginResponseDto;
import com.library.dto.user.UserResponseDto;
import com.library.exception.BadRequestException;
import com.library.repository.AccountRepository;
import com.library.repository.UserRepository;
import com.library.requestform.account.LoginRequestForm;
import com.library.requestform.account.RegisterRequestForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testRegisterSuccessfullyCreatesAccountAndUser() {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis()).substring(6);
        RegisterRequestForm form = RegisterRequestForm.builder()
                .username("testuser_" + uniqueSuffix)
                .password("TestPass@123")
                .email("test_" + uniqueSuffix + "@library.com")
                .phone("0987" + uniqueSuffix)
                .fullName("Test Reader " + uniqueSuffix)
                .build();

        UserResponseDto response = authService.register(form);

        assertNotNull(response);
        assertNotNull(response.getAccountId());
        assertNotNull(response.getUserId());
        assertEquals("testuser_" + uniqueSuffix, response.getUsername());
        assertEquals("test_" + uniqueSuffix + "@library.com", response.getEmail());

        assertTrue(accountRepository.findByUsername("testuser_" + uniqueSuffix).isPresent());
        assertTrue(userRepository.findByEmail("test_" + uniqueSuffix + "@library.com").isPresent());
    }

    @Test
    void testRegisterDuplicateUsernameThrowsException() {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis()).substring(6);
        RegisterRequestForm form = RegisterRequestForm.builder()
                .username("dupuser_" + uniqueSuffix)
                .password("TestPass@123")
                .email("dup1_" + uniqueSuffix + "@library.com")
                .phone("0981" + uniqueSuffix)
                .fullName("User One")
                .build();

        authService.register(form);

        // Đăng ký lại cùng username nhưng email khác
        RegisterRequestForm form2 = RegisterRequestForm.builder()
                .username("dupuser_" + uniqueSuffix)
                .password("TestPass@123")
                .email("dup2_" + uniqueSuffix + "@library.com")
                .phone("0982" + uniqueSuffix)
                .fullName("User Two")
                .build();

        assertThrows(BadRequestException.class, () -> authService.register(form2));
    }

    @Test
    void testLoginSuccessfullyReturnsTokens() {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis()).substring(6);
        String username = "login_user_" + uniqueSuffix;
        String rawPassword = "TestPass@123";

        RegisterRequestForm registerForm = RegisterRequestForm.builder()
                .username(username)
                .password(rawPassword)
                .email("login_" + uniqueSuffix + "@library.com")
                .phone("0977" + uniqueSuffix)
                .fullName("Login User")
                .build();
        authService.register(registerForm);

        LoginRequestForm loginForm = LoginRequestForm.builder()
                .username(username)
                .password(rawPassword)
                .build();

        LoginResponseDto loginResponse = authService.login(loginForm);

        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getAccessToken());
        assertNotNull(loginResponse.getRefreshToken());
    }

    @Test
    void testLoginWithWrongPasswordThrowsException() {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis()).substring(6);
        String username = "wrongpw_user_" + uniqueSuffix;

        RegisterRequestForm registerForm = RegisterRequestForm.builder()
                .username(username)
                .password("TestPass@123")
                .email("wrongpw_" + uniqueSuffix + "@library.com")
                .phone("0978" + uniqueSuffix)
                .fullName("Wrong Password User")
                .build();
        authService.register(registerForm);

        LoginRequestForm loginForm = LoginRequestForm.builder()
                .username(username)
                .password("WrongPassword@999")
                .build();

        assertThrows(BadRequestException.class, () -> authService.login(loginForm));
    }

    @Test
    void testLoginWithNonExistentUsernameThrowsException() {
        LoginRequestForm loginForm = LoginRequestForm.builder()
                .username("non_existent_username_123456")
                .password("TestPass@123")
                .build();

        assertThrows(BadRequestException.class, () -> authService.login(loginForm));
    }
}
