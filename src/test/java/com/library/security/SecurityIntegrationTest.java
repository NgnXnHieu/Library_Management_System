package com.library.security;

import com.library.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtServiceImpl jwtService;

    @Test
    void testPublicEndpointAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpointWithoutTokenReturnsUnauthorizedApiResponse() throws Exception {
        mockMvc.perform(get("/roles"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn!"));
    }

    @Test
    void testProtectedEndpointWithValidTokenInCookie() throws Exception {
        String token = "valid.test.token";
        Long accountId = 1L;

        UserDetailCustom userDetails = UserDetailCustom.builder()
                .accountId(accountId)
                .userId(10L)
                .roleCode("ADMIN")
                .username("admin_test")
                .fullName("Quản trị viên")
                .build();

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.isAccessToken(token)).thenReturn(true);
        when(jwtService.extractAccountId(token)).thenReturn(accountId);
        when(jwtService.getUserDetailsByAccountId(accountId, token)).thenReturn(userDetails);

        mockMvc.perform(get("/roles")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, token)))
                .andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpointWithValidTokenInAuthorizationHeader() throws Exception {
        String token = "valid.test.token.header";
        Long accountId = 2L;

        UserDetailCustom userDetails = UserDetailCustom.builder()
                .accountId(accountId)
                .userId(20L)
                .roleCode("LIBRARIAN")
                .username("librarian_test")
                .fullName("Thủ thư")
                .build();

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.isAccessToken(token)).thenReturn(true);
        when(jwtService.extractAccountId(token)).thenReturn(accountId);
        when(jwtService.getUserDetailsByAccountId(accountId, token)).thenReturn(userDetails);

        mockMvc.perform(get("/roles")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, SecurityConstants.BEARER_PREFIX + token))
                .andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpointWithInvalidTokenReturnsUnauthorized() throws Exception {
        String invalidToken = "invalid.token";

        when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        mockMvc.perform(get("/roles")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, SecurityConstants.BEARER_PREFIX + invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    /**
     * Kiểm tra endpoint dành riêng cho ADMIN (/categories - POST)
     * khi tài khoản có Role CUSTOMER gọi sẽ bị chặn với mã HTTP 403 FORBIDDEN ngay tại Security Filter.
     */
    @Test
    void testAdminPostEndpointAccessedByCustomerReturnsForbidden() throws Exception {
        String token = "customer.token";
        Long accountId = 99L;

        UserDetailCustom customerDetails = UserDetailCustom.builder()
                .accountId(accountId)
                .userId(99L)
                .roleCode("CUSTOMER")
                .username("customer_test")
                .fullName("Khách hàng")
                .build();

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.isAccessToken(token)).thenReturn(true);
        when(jwtService.extractAccountId(token)).thenReturn(accountId);
        when(jwtService.getUserDetailsByAccountId(accountId, token)).thenReturn(customerDetails);

        mockMvc.perform(post("/categories")
                        .cookie(new Cookie(SecurityConstants.ACCESS_TOKEN_COOKIE_NAME, token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }
}
