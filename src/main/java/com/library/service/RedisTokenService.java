package com.library.service;

import java.time.Duration;

/**
 * Interface định nghĩa các nghiệp vụ lưu trữ, kiểm tra và vô hiệu hóa Token trên Redis Cache.
 */
public interface RedisTokenService {

    /**
     * Lưu Access Token vào Redis với thời gian sống (TTL).
     *
     * @param accountId ID tài khoản của người dùng
     * @param token     Chuỗi Access Token JWT
     * @param ttl       Thời gian tồn tại của token
     */
    void saveAccessToken(Long accountId, String token, Duration ttl);

    /**
     * Lưu Refresh Token vào Redis với thời gian sống (TTL).
     *
     * @param accountId    ID tài khoản của người dùng
     * @param refreshToken Chuỗi Refresh Token JWT
     * @param ttl          Thời gian tồn tại của refresh token
     */
    void saveRefreshToken(Long accountId, String refreshToken, Duration ttl);

    /**
     * Lấy Access Token đang hoạt động của tài khoản từ Redis.
     *
     * @param accountId ID tài khoản
     * @return Chuỗi token hoặc null nếu không tồn tại / đã hết hạn
     */
    String getAccessToken(Long accountId);

    /**
     * Lấy Refresh Token đang hoạt động của tài khoản từ Redis.
     *
     * @param accountId ID tài khoản
     * @return Chuỗi refresh token hoặc null nếu không tồn tại / đã hết hạn
     */
    String getRefreshToken(Long accountId);

    /**
     * Xóa toàn bộ token của tài khoản trên Redis (dùng khi đăng xuất hoặc thu hồi phiên).
     *
     * @param accountId ID tài khoản
     */
    void deleteTokens(Long accountId);

    /**
     * Kiểm tra xem tài khoản có Access Token hợp lệ trong Redis hay không.
     *
     * @param accountId ID tài khoản
     * @return true nếu còn token, ngược lại false
     */
    boolean hasAccessToken(Long accountId);
}
