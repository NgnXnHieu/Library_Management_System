package com.library.service.impl;

import com.library.dto.auth.LoginResponseDto;
import com.library.dto.user.UserResponseDto;
import com.library.entity.Account;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.enums.AccountStatus;
import com.library.exception.BadRequestException;
import com.library.mapper.AccountMapper;
import com.library.mapper.UserMapper;
import com.library.repository.AccountRepository;
import com.library.repository.RoleRepository;
import com.library.repository.UserRepository;
import com.library.requestform.account.LoginRequestForm;
import com.library.requestform.account.RegisterRequestForm;
import com.library.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper;
    private final UserMapper userMapper;
    private final JwtServiceImpl jwtService;

    @Override
    @Transactional
    public UserResponseDto register(RegisterRequestForm form) {
        // 1. Kiểm tra username đã tồn tại chưa qua AccountRepository
        if (accountRepository.existsByUsername(form.getUsername())) {
            throw new BadRequestException("Tài khoản '" + form.getUsername() + "' đã tồn tại trên hệ thống!");
        }

        // 2. Kiểm tra email đã được đăng ký chưa qua UserRepository
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new BadRequestException("Email '" + form.getEmail() + "' đã được đăng ký!");
        }

        // 3. Kiểm tra số điện thoại đã được đăng ký chưa qua UserRepository
        if (userRepository.existsByPhone(form.getPhone())) {
            throw new BadRequestException("Số điện thoại '" + form.getPhone() + "' đã được đăng ký!");
        }

        // 4. Tạo và lưu Account mới
        Account account = accountMapper.toEntity(form);
        account.setUsername(form.getUsername().trim());
        account.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        Account savedAccount = accountRepository.save(account);

        // 5. Lấy vai trò mặc định từ RoleRepository
        Role defaultRole = roleRepository.findDefaultRole()
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .code("CUSTOMER")
                                .name("Khách hàng")
                                .description("Vai trò mặc định khi đăng ký tài khoản")
                                .build()
                ));

        // 6. Tạo và lưu User mới
        User user = userMapper.toEntity(form);
        user.setAccount(savedAccount);
        user.setRole(defaultRole);
        user.setStatus(AccountStatus.ACTIVE);
        String fullName = (form.getFullName() != null && !form.getFullName().trim().isEmpty())
                ? form.getFullName().trim()
                : form.getUsername().trim();
        user.setFullName(fullName);
        User savedUser = userRepository.save(user);

        // 7. Map từ User sang UserResponseDto bằng UserMapper
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestForm form) {
        // 1. Kiểm tra tài khoản có tồn tại không
        Account account = accountRepository.findByUsername(form.getUsername().trim())
                .orElseThrow(() -> new BadRequestException("Tài khoản hoặc mật khẩu không chính xác!"));

        // 2. Kiểm tra mật khẩu có khớp không
        if (!passwordEncoder.matches(form.getPassword(), account.getPasswordHash())) {
            throw new BadRequestException("Tài khoản hoặc mật khẩu không chính xác!");
        }

        // 3. Kiểm tra trạng thái Account có ACTIVE không
        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new BadRequestException("Tài khoản của bạn đã bị khóa hoặc chưa được kích hoạt!");
        }

        // Kiểm tra thêm trạng thái User
        User user = account.getUser();
        if (user != null && user.getStatus() != null && user.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Tài khoản người dùng đã bị khóa hoặc ngừng hoạt động!");
        }

        // 4. Cập nhật thời gian đăng nhập gần nhất
        account.setLastLoginAt(LocalDateTime.now());
        accountRepository.save(account);

        // 5. Tạo JWT token chứa accountId, thời gian hết hạn, loại token
        String accessToken = jwtService.generateAccessToken(account.getId());
        String refreshToken = jwtService.generateRefreshToken(account.getId());

        // 6. Trả về thông tin accessToken và refreshToken
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
