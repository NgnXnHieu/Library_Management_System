package com.library.service;

import com.library.dto.account.AccountResponseDto;

import java.util.List;

public interface AccountService {

    AccountResponseDto getAccountById(Long id);

    AccountResponseDto getAccountByUsername(String username);

    List<AccountResponseDto> getAllAccounts();

    void updateAccountStatus(Long id, String status);
}
