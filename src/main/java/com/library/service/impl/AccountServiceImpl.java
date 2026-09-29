package com.library.service.impl;

import com.library.dto.account.AccountResponseDto;
import com.library.entity.Account;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.AccountMapper;
import com.library.repository.AccountRepository;
import com.library.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + id));
        return accountMapper.toDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getAccountByUsername(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với tên đăng nhập: " + username));
        return accountMapper.toDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accountMapper.toDtoList(accounts);
    }

    @Override
    @Transactional
    public void updateAccountStatus(Long id, String status) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + id));
        account.setStatus(status);
        accountRepository.save(account);
    }
}
