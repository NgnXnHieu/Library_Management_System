package com.library.service;

import com.library.dto.account.AccountResponseDto;
import com.library.dto.user.UserResponseDto;
import com.library.requestform.account.AccountCreateRequestForm;
import com.library.requestform.account.RegisterRequestForm;

import java.util.List;

public interface AccountService {

    AccountResponseDto getAccountById(Long id);

    AccountResponseDto getAccountByUsername(String username);

    List<AccountResponseDto> getAllAccounts();

    void updateAccountStatus(Long id, String status);

    /**
     * Tạo tài khoản mới từ phía Admin với vai trò và chi nhánh chỉ định.
     *
     * @param form Thông tin tạo tài khoản
     * @return UserResponseDto thông tin người dùng vừa được tạo
     */
    UserResponseDto createAccountByAdmin(AccountCreateRequestForm form);

    /**
     * Tạo tài khoản mới cho khách hàng (Role CUSTOMER, không gán chi nhánh).
     * Dành cho Admin, BranchManager và Staff.
     *
     * @param form Thông tin đăng ký tài khoản khách hàng
     * @return UserResponseDto thông tin người dùng vừa được tạo
     */
    UserResponseDto createCustomerAccount(RegisterRequestForm form);
}

