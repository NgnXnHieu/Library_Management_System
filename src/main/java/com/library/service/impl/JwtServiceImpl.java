package com.library.service.impl;

import com.library.entity.Account;
import com.library.entity.User;
import com.library.enums.AccountStatus;
import com.library.exception.BadRequestException;
import com.library.repository.AccountRepository;
import com.library.repository.UserRepository;
import com.library.security.UserDetailCustom;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtServiceImpl {
    private final SecretKey signingKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Autowired
    public JwtServiceImpl(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-expiration}") long refreshTokenExpiration,
            AccountRepository accountRepository,
            UserRepository userRepository
    ) {

        this.signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );

        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    /**
     * Constructor phục vụ cho các unit test chỉ kiểm tra mã hóa/giải mã token
     */
    public JwtServiceImpl(
            String secret,
            long accessTokenExpiration,
            long refreshTokenExpiration
    ) {
        this(secret, accessTokenExpiration, refreshTokenExpiration, null, null);
    }


    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    /**
     * Tạo ResponseCookie cho access token với thời gian sống trùng với accessTokenExpiration
     */
    public ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(false) // Đổi thành true khi chạy HTTPS production
                .path("/")
                .maxAge(Duration.ofMillis(accessTokenExpiration))
                .sameSite("Lax")
                .build();
    }

    /**
     * Tạo ResponseCookie cho refresh token với thời gian sống trùng với refreshTokenExpiration
     */
    public ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false) // Đổi thành true khi chạy HTTPS production
                .path("/")
                .maxAge(Duration.ofMillis(refreshTokenExpiration))
                .sameSite("Lax")
                .build();
    }

    /**
     * Tạo access token chứa accountId, tokenType: ACCESS, thời gian hết hạn
     */
    public String generateAccessToken(Long accountId) {
        return Jwts.builder()
                .subject(String.valueOf(accountId))
                .claim("accountId", accountId)
                .claim("tokenType", "ACCESS")
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis()
                                + accessTokenExpiration)
                )
                .signWith(signingKey)
                .compact();
    }

    /**
     * Tạo refresh token chứa accountId, tokenType: REFRESH, thời gian hết hạn
     */
    public String generateRefreshToken(Long accountId) {
        return Jwts.builder()
                .subject(String.valueOf(accountId))
                .claim("accountId", accountId)
                .claim("tokenType", "REFRESH")
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis()
                                + refreshTokenExpiration)
                )
                .signWith(signingKey)
                .compact();
    }

    /**
     * Lấy accountId từ token
     */
    public Long extractAccountId(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        return subject != null ? Long.valueOf(subject) : null;
    }

    /**
     * Lấy loại token (ACCESS hoặc REFRESH)
     */
    public String extractTokenType(String token) {
        return extractClaim(
                token,
                claims -> claims.get("tokenType", String.class)
        );
    }

    /**
     * Lấy expiration
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Lấy một claim bất kỳ
     */
    public <T> T extractClaim(
            String token,
            Function<Claims, T> resolver
    ) {
        Claims claims = getClaims(token);
        return resolver.apply(claims);
    }

    /**
     * Parse + verify signature + lấy claims
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Kiểm tra token hết hạn chưa (bảo vệ tránh văng lỗi khi token hết hạn)
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Kiểm tra token hợp lệ (chữ ký chuẩn, chưa hết hạn)
     */
    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Kiểm tra access token
     */
    public boolean isAccessToken(String token) {
        try {
            return "ACCESS".equals(extractTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Kiểm tra refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            return "REFRESH".equals(extractTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Xác thực Account và User từ accountId, lấy đầy đủ branchId và roleCode để gán vào UserDetailCustom
     * phục vụ lưu trữ vào SecurityContext sau này.
     *
     * @param accountId ID của tài khoản
     * @return UserDetailCustom chứa accountId, userId, branchId, roleCode
     */
    @Transactional(readOnly = true)
    public UserDetailCustom getUserDetailsByAccountId(Long accountId) {
        if (accountId == null) {
            throw new BadRequestException("AccountId không được để trống!");
        }

        if (accountRepository == null || userRepository == null) {
            throw new IllegalStateException("AccountRepository hoặc UserRepository chưa được khởi tạo trong JwtServiceImpl!");
        }

        // 1. Kiểm tra xem account có tồn tại và có active không
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản với ID: " + accountId));

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new BadRequestException("Tài khoản đã bị khóa hoặc chưa được kích hoạt!");
        }

        // 2. Kiểm tra xem user chứa accountId đó có tồn tại và có active không (join fetch role và branch)
        User user = userRepository.findByAccountIdWithDetails(accountId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy thông tin người dùng liên kết với tài khoản ID: " + accountId));

        if (user.getStatus() != null && user.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Thông tin người dùng đã bị khóa hoặc ngừng hoạt động!");
        }

        // 3. Trích xuất branchId (nếu có) và roleCode (nếu có)
        Long branchId = (user.getBranch() != null) ? user.getBranch().getId() : null;
        String roleCode = (user.getRole() != null) ? user.getRole().getCode() : null;

        // 4. Gán vào UserDetailCustom
        return UserDetailCustom.builder()
                .accountId(account.getId())
                .userId(user.getId())
                .branchId(branchId)
                .roleCode(roleCode)
                .username(account.getUsername())
                .fullName(user.getFullName())
                .build();
    }
}

