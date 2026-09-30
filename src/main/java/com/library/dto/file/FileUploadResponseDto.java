package com.library.dto.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO trả về thông tin sau khi upload tệp tin thành công.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadResponseDto {

    /**
     * Tên gốc của tệp tin tải lên
     */
    private String originalFileName;

    /**
     * Khóa định danh file tương đối (dùng lưu vào Database, ví dụ: "books/uuid.jpg")
     */
    private String fileKey;

    /**
     * Đường dẫn URL xem ảnh (ví dụ: "/api/uploads/books/uuid.jpg")
     */
    private String fileUrl;
}
