package com.library.service.impl;

import com.library.dto.account.AccountResponseDto;
import com.library.dto.user.UserResponseDto;
import com.library.entity.Account;
import com.library.entity.Branch;
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
import com.library.requestform.account.AccountCreateRequestForm;
import com.library.requestform.account.RegisterRequestForm;
import com.library.service.AccountService;
import com.library.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service xử lý các nghiệp vụ liên quan đến tài khoản (Account).
 */
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_BY_ID, id));
        return accountMapper.toDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getAccountByUsername(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_BY_USERNAME, username));
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
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_BY_ID, id));
        account.setStatus(status);
        accountRepository.save(account);
    }

    /**
     * Tạo tài khoản mới từ phía Admin với vai trò và chi nhánh chỉ định.
     *
     * @param form Thông tin tạo tài khoản từ Admin
     * @return UserResponseDto thông tin chi tiết người dùng và tài khoản vừa tạo
     */
    @Override
    @Transactional
    public UserResponseDto createAccountByAdmin(AccountCreateRequestForm form) {

        // Bước 2: Kiểm tra dữ liệu trùng lặp (username, email, số điện thoại)
        if (accountRepository.existsByUsername(form.getUsername().trim())) {
            throw new AppException(ErrorCode.ACCOUNT_ALREADY_EXISTS, form.getUsername().trim());
        }
        if (userRepository.existsByEmail(form.getEmail().trim())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, form.getEmail().trim());
        }
        if (userRepository.existsByPhone(form.getPhone().trim())) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS, form.getPhone().trim());
        }

        // Bước 3: Tìm kiếm vai trò (Role) trong hệ thống theo mã roleCode từ form
        String roleCode = form.getRole().name();
        Role roleEntity = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND, roleCode));

        // Bước 4: Tìm kiếm chi nhánh (Branch) theo branchId được truyền vào
        Branch branchEntity = branchRepository.findById(form.getBranchId())
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND, form.getBranchId()));

        // Bước 5: Tạo mới và lưu thông tin Account vào Database
        Account account = Account.builder()
                .username(form.getUsername().trim())
                .passwordHash(passwordEncoder.encode(form.getPassword()))
                .status(form.getStatus().name())
                .build();
        Account savedAccount = accountRepository.save(account);

        // Bước 6: Tạo mới và lưu thông tin User vào Database (lưu accountId, role,
        // branch và các thông tin cá nhân)
        User user = User.builder()
                .account(savedAccount)
                .role(roleEntity)
                .branch(branchEntity)
                .fullName(form.getFullName().trim())
                .email(form.getEmail().trim())
                .phone(form.getPhone().trim())
                .status(form.getStatus())
                .build();
        User savedUser = userRepository.save(user);

        // Bước 7: Chuyển đổi User entity sang UserResponseDto và trả về kết quả
        return userMapper.toDto(savedUser);
    }

    /**
     * Tạo tài khoản mới cho khách hàng (Role CUSTOMER, không gán chi nhánh).
     * Dành cho Admin, BranchManager và Staff.
     *
     * @param form Thông tin đăng ký tài khoản khách hàng
     * @return UserResponseDto thông tin chi tiết người dùng và tài khoản vừa tạo
     */
    @Override
    @Transactional
    public UserResponseDto createCustomerAccount(RegisterRequestForm form) {
        // Bước 1: Kiểm tra dữ liệu trùng lặp (username, email, số điện thoại)
        if (accountRepository.existsByUsername(form.getUsername().trim())) {
            throw new AppException(ErrorCode.ACCOUNT_ALREADY_EXISTS, form.getUsername().trim());
        }
        if (userRepository.existsByEmail(form.getEmail().trim())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, form.getEmail().trim());
        }
        if (userRepository.existsByPhone(form.getPhone().trim())) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS, form.getPhone().trim());
        }

        // Bước 2: Tìm kiếm vai trò CUSTOMER trong hệ thống
        Role customerRole = roleRepository.findByCode("CUSTOMER")
                .orElseGet(() -> roleRepository.findDefaultRole()
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND, "CUSTOMER")));

        // Bước 3: Tạo mới và lưu thông tin Account vào Database với mật khẩu được mã hóa
        Account account = Account.builder()
                .username(form.getUsername().trim())
                .passwordHash(passwordEncoder.encode(form.getPassword()))
                .status(AccountStatus.ACTIVE.name())
                .build();
        Account savedAccount = accountRepository.save(account);

        // Bước 4: Tạo mới và lưu thông tin User vào Database (không gán chi nhánh, role là CUSTOMER)
        String fullName = (form.getFullName() != null && !form.getFullName().trim().isEmpty())
                ? form.getFullName().trim()
                : form.getUsername().trim();

        User user = User.builder()
                .account(savedAccount)
                .role(customerRole)
                .branch(null)
                .fullName(fullName)
                .email(form.getEmail().trim())
                .phone(form.getPhone().trim())
                .status(AccountStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        // Bước 5: Chuyển đổi User entity sang UserResponseDto và trả về kết quả
        return userMapper.toDto(savedUser);
    }
}
