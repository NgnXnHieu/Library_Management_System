package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.auth.LoginResponseDto;
import com.library.requestform.account.LoginRequestForm;
import com.library.service.AuthService;
import com.library.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtServiceImpl jwtService;

    @Test
    void testLoginSuccessSetsCookies() throws Exception {
        LoginRequestForm form = LoginRequestForm.builder()
                .username("testuser123")
                .password("Password@123")
                .build();

        LoginResponseDto responseDto = LoginResponseDto.builder()
                .accessToken("mock.access.token")
                .refreshToken("mock.refresh.token")
                .build();

        when(authService.login(any(LoginRequestForm.class))).thenReturn(responseDto);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "mock.access.token")
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMillis(86400000))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "mock.refresh.token")
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMillis(604800000))
                .build();

        when(jwtService.createAccessTokenCookie(eq("mock.access.token"))).thenReturn(accessCookie);
        when(jwtService.createRefreshTokenCookie(eq("mock.refresh.token"))).thenReturn(refreshCookie);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(cookie().value("accessToken", "mock.access.token"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().value("refreshToken", "mock.refresh.token"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
