package com.library.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình OpenAPI (Swagger 3) cho hệ thống Quản lý thư viện.
 * Cung cấp tài liệu API trực quan và tích hợp xác thực JWT Bearer Token.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    /**
     * Khởi tạo cấu hình OpenAPI với thông tin dự án và cơ chế bảo mật Bearer Token.
     *
     * @return Đối tượng cấu hình OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Bước 1: Cấu hình thông tin mô tả dự án
                .info(new Info()
                        .title("Library Management System API")
                        .description("Tài liệu RESTful API hệ thống Quản lý Thư viện (Library Management System)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Đội ngũ phát triển Thư viện")
                                .email("support@library.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")))
                // Bước 2: Kích hoạt Security Requirement toàn cục cho Swagger UI (hiển thị biểu tượng ổ khóa / Authorize)
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                // Bước 3: Định nghĩa Security Scheme loại HTTP Bearer JWT
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Nhập mã Access Token JWT của bạn vào đây (không cần gõ tiền tố 'Bearer ')")));
    }
}
