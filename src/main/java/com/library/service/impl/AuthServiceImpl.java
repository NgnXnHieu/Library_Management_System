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
import com.library.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
                                .build()));

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

        // 4. Tạo JWT token chứa accountId, thời gian hết hạn, loại token
        String accessToken = jwtService.generateAccessToken(account.getId());
        String refreshToken = jwtService.generateRefreshToken(account.getId());

        // 5. Cập nhật thời gian đăng nhập gần nhất và lưu token, refreshToken vào
        // database
        account.setLastLoginAt(LocalDateTime.now());
        account.setToken(accessToken);
        account.setRefreshToken(refreshToken);
        accountRepository.save(account);

        // 6. Trích xuất Role và họ tên của User để trả về cho Frontend
        String roleCode = "ROLE_CUSTOMER";
        if (user != null && user.getRole() != null && user.getRole().getCode() != null) {
            String code = user.getRole().getCode().trim().toUpperCase();
            roleCode = code.startsWith("ROLE_") ? code : "ROLE_" + code;
        }

        String fullName = (user != null && user.getFullName() != null) ? user.getFullName() : account.getUsername();

        // 7. Trả về thông tin đăng nhập: username, fullName, role và token (dùng cho Cookie)
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(account.getUsername())
                .fullName(fullName)
                .role(roleCode)
                .build();
    }

    /**
     * Xử lý đăng xuất:
     * - Dùng SecurityUtil lấy accountId đang đăng nhập.
     * - Xóa token và refreshToken trong DB để vô hiệu hóa phiên làm việc.
     * - Xóa thông tin xác thực trong SecurityContextHolder.
     */
    @Override
    @Transactional
    public void logout() {
        // Bước 1: Lấy accountId của người dùng hiện tại từ SecurityUtil (sẽ ném
        // exception 401 nếu chưa đăng nhập)
        Long currentAccountId = SecurityUtil.getRequiredAccountId();

        // Bước 2: Tìm kiếm tài khoản trong database
        Account account = accountRepository.findById(currentAccountId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản với ID: " + currentAccountId));

        // Bước 3: Xóa token và refreshToken đã lưu trong tài khoản để vô hiệu hóa
        account.setToken(null);
        account.setRefreshToken(null);
        accountRepository.save(account);

        // Bước 4: Xóa sạch thông tin xác thực trong SecurityContextHolder
        SecurityContextHolder.clearContext();
    }

    /**
     * Làm mới token (Refresh Token):
     * - Kiểm tra chuỗi token không rỗng và đúng định dạng JWT
     * - Kiểm tra chữ ký và hạn sử dụng chưa hết
     * - Kiểm tra đúng loại tokenType là REFRESH
     * - Kiểm tra tài khoản (Account) tồn tại và ACTIVE
     * - Kiểm tra khớp với chuỗi refreshToken đang lưu trong Database
     * - Kiểm tra thông tin người dùng (User) liên kết tồn tại và ACTIVE
     * - Tạo mới cặp accessToken và refreshToken (Refresh Token Rotation)
     * - Cập nhật cặp token mới vào Database
     * - Trả về DTO thông tin tài khoản kèm cặp token mới để Controller ghi vào Cookie
     */
    @Override
    @Transactional
    public LoginResponseDto refreshToken(String refreshToken) {
        // Bước 1: Kiểm tra chuỗi refreshToken gửi lên có hợp lệ không
        if (!StringUtils.hasText(refreshToken)) {
            throw new BadRequestException("Refresh token không được để trống hoặc không tìm thấy trong Cookie!");
        }

        // Bước 2: Kiểm tra chữ ký và thời hạn của token
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new BadRequestException("Phiên đăng nhập đã hết hạn hoặc Refresh token không hợp lệ! Vui lòng đăng nhập lại.");
        }

        // Bước 3: Kiểm tra loại token phải là REFRESH
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BadRequestException("Mã token được gửi lên không phải là Refresh token hợp lệ!");
        }

        // Bước 4: Trích xuất accountId từ claims của token
        Long accountId = jwtService.extractAccountId(refreshToken);
        if (accountId == null) {
            throw new BadRequestException("Không thể xác thực thông tin tài khoản từ Refresh token!");
        }

        // Bước 5: Tìm kiếm tài khoản trong DB và kiểm tra trạng thái
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản liên kết với token này!"));

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new BadRequestException("Tài khoản của bạn đã bị khóa hoặc chưa được kích hoạt!");
        }

        // Bước 6: Kiểm tra token gửi lên có khớp với refreshToken lưu trong DB không
        if (account.getRefreshToken() == null || !account.getRefreshToken().equals(refreshToken.trim())) {
            throw new BadRequestException("Phiên đăng nhập không hợp lệ hoặc tài khoản đã đăng nhập ở thiết bị khác!");
        }

        // Bước 7: Kiểm tra thông tin người dùng (User) liên kết
        User user = userRepository.findByAccountIdWithDetails(accountId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy thông tin người dùng liên kết với tài khoản!"));

        if (user.getStatus() != null && user.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Tài khoản người dùng đã bị khóa hoặc ngừng hoạt động!");
        }

        // Bước 8: Tạo cặp Access Token và Refresh Token mới (Refresh Token Rotation)
        String newAccessToken = jwtService.generateAccessToken(account.getId());
        String newRefreshToken = jwtService.generateRefreshToken(account.getId());

        // Bước 9: Cập nhật token mới vào database
        account.setToken(newAccessToken);
        account.setRefreshToken(newRefreshToken);
        accountRepository.save(account);

        // Bước 10: Trích xuất Role và họ tên của User để trả về
        String roleCode = "ROLE_CUSTOMER";
        if (user.getRole() != null && user.getRole().getCode() != null) {
            String code = user.getRole().getCode().trim().toUpperCase();
            roleCode = code.startsWith("ROLE_") ? code : "ROLE_" + code;
        }

        String fullName = (user.getFullName() != null) ? user.getFullName() : account.getUsername();

        return LoginResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .username(account.getUsername())
                .fullName(fullName)
                .role(roleCode)
                .build();
    }
}
