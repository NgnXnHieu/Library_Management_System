package com.library.service.impl;

import com.library.service.RedisTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Triển khai dịch vụ quản lý Token lưu trữ trên Redis Cache.
 * Hỗ trợ lưu trữ, truy xuất và vô hiệu hóa Token kèm TTL (Time-To-Live).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenServiceImpl implements RedisTokenService {

    // Tiền tố định danh key cho Access Token trên Redis
    private static final String ACCESS_TOKEN_PREFIX = "auth:token:access:";

    // Tiền tố định danh key cho Refresh Token trên Redis
    private static final String REFRESH_TOKEN_PREFIX = "auth:token:refresh:";

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Lưu Access Token vào Redis với thời gian sống (TTL).
     */
    @Override
    public void saveAccessToken(Long accountId, String token, Duration ttl) {
        // Bước 1: Tạo key theo định dạng auth:token:access:{accountId}
        String key = ACCESS_TOKEN_PREFIX + accountId;

        // Bước 2: Lưu token vào Redis kèm TTL tự hủy
        redisTemplate.opsForValue().set(key, token, ttl);
        log.debug("Đã lưu Access Token vào Redis cho Account ID [{}], TTL: [{} giây]", accountId, ttl.getSeconds());
    }

    /**
     * Lưu Refresh Token vào Redis với thời gian sống (TTL).
     */
    @Override
    public void saveRefreshToken(Long accountId, String refreshToken, Duration ttl) {
        // Bước 1: Tạo key theo định dạng auth:token:refresh:{accountId}
        String key = REFRESH_TOKEN_PREFIX + accountId;

        // Bước 2: Lưu refreshToken vào Redis kèm TTL tự hủy
        redisTemplate.opsForValue().set(key, refreshToken, ttl);
        log.debug("Đã lưu Refresh Token vào Redis cho Account ID [{}], TTL: [{} giây]", accountId, ttl.getSeconds());
    }

    /**
     * Lấy Access Token đang hoạt động của tài khoản từ Redis.
     */
    @Override
    public String getAccessToken(Long accountId) {
        String key = ACCESS_TOKEN_PREFIX + accountId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Lấy Refresh Token đang hoạt động của tài khoản từ Redis.
     */
    @Override
    public String getRefreshToken(Long accountId) {
        String key = REFRESH_TOKEN_PREFIX + accountId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Xóa toàn bộ token của tài khoản trên Redis khi đăng xuất hoặc thu hồi phiên.
     */
    @Override
    public void deleteTokens(Long accountId) {
        String accessKey = ACCESS_TOKEN_PREFIX + accountId;
        String refreshKey = REFRESH_TOKEN_PREFIX + accountId;

        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);
        log.debug("Đã xóa token khỏi Redis cho Account ID [{}]", accountId);
    }

    /**
     * Kiểm tra xem tài khoản có Access Token còn hạn trong Redis hay không.
     */
    @Override
    public boolean hasAccessToken(Long accountId) {
        String key = ACCESS_TOKEN_PREFIX + accountId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
