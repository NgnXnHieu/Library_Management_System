package com.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Cấu hình kết nối và bộ tuần tự hóa (Serializer) cho Redis.
 * Sử dụng StringRedisSerializer cho cả Key và Value để dữ liệu hiển thị dạng text rõ ràng,
 * không bị lỗi mã hóa nhị phân mặc định của Java (\xac\xed\x00\x05...).
 */
@Configuration
public class RedisConfig {

    /**
     * Cấu hình RedisTemplate dùng để lưu trữ và truy vấn chuỗi Token (String).
     *
     * @param connectionFactory Nhà cung cấp kết nối Redis (mặc định Lettuce)
     * @return RedisTemplate<String, String> đã cấu hình Serializer chuẩn
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Thiết lập Serializer cho Key và Value là chuỗi String thuần túy
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
