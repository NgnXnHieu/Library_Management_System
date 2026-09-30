package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.user.UserResponseDto;
import com.library.requestform.account.AccountCreateRequestForm;
import com.library.requestform.account.RegisterRequestForm;
import com.library.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller tiếp nhận và xử lý các yêu cầu liên quan đến tài khoản (Account).
 */
@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * API tạo tài khoản phía Admin với vai trò và chi nhánh chỉ định.
     *
     * @param form Dữ liệu đầu vào tạo tài khoản gồm username, password, fullName, role, status, branchId, email, phone
     * @return Thông tin người dùng vừa được tạo bọc trong chuẩn ApiResponse
     */
    @PostMapping("/accounts/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> createAccountByAdmin(
            @Valid @RequestBody AccountCreateRequestForm form) {
        // Bước 1: Gọi xuống tầng Service để xử lý nghiệp vụ tạo tài khoản
        UserResponseDto responseDto = accountService.createAccountByAdmin(form);

        // Bước 2: Bọc kết quả trả về trong chuẩn ApiResponse với HTTP status 201 CREATED
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo tài khoản thành công!", responseDto));
    }

    /**
     * API tạo tài khoản cho khách hàng dành cho Admin, BranchManager và Staff.
     * Không gán chi nhánh và mặc định role là CUSTOMER.
     *
     * @param form Dữ liệu đầu vào đăng ký tài khoản khách hàng
     * @return Thông tin người dùng vừa được tạo bọc trong chuẩn ApiResponse
     */
    @PostMapping("/accounts/customer")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCHMANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<UserResponseDto>> createCustomerAccount(
            @Valid @RequestBody RegisterRequestForm form) {
        // Bước 1: Gọi xuống tầng Service để xử lý nghiệp vụ tạo tài khoản khách hàng
        UserResponseDto responseDto = accountService.createCustomerAccount(form);

        // Bước 2: Bọc kết quả trả về trong chuẩn ApiResponse với HTTP status 201 CREATED
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo tài khoản khách hàng thành công!", responseDto));
    }
}
