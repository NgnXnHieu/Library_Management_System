package com.library.config;

import com.library.util.FileUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Cấu hình Spring WebMvc phục vụ các tệp tĩnh (ảnh tải lên).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get(FileUtil.UPLOAD_DIR).toAbsolutePath().normalize();
        String uploadPath = uploadDir.toUri().toString();

        // Ánh xạ URL /uploads/** tới thư mục thực tế trên ổ đĩa
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath.endsWith("/") ? uploadPath : uploadPath + "/");
    }
}
