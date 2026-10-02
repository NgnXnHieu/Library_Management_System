package com.library.util;

import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Lớp tiện ích xử lý lưu trữ, kiểm tra và xóa file ảnh trên hệ thống.
 */
public final class FileUtil {

    /**
     * Thư mục gốc lưu trữ tệp tải lên
     */
    public static final String UPLOAD_DIR = "uploads";

    /**
     * Danh sách định dạng ảnh hợp lệ
     */
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif");

    /**
     * Danh sách Content-Type ảnh hợp lệ
     */
    private static final List<String> ALLOWED_IMAGE_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    /**
     * Kích thước file tối đa cho phép (10MB)
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private FileUtil() {
        // Utility class
    }

    /**
     * Lưu file tải lên vào thư mục con chỉ định.
     *
     * @param file      Tệp tin tải lên từ client
     * @param subFolder Thư mục con (ví dụ: "books", "users")
     * @return Khóa định danh file tương đối (fileKey), ví dụ: "books/uuid.jpg"
     */
    public static String saveFile(MultipartFile file, String subFolder) {
        // Bước 1: Kiểm tra tính hợp lệ của file
        validateImageFile(file);

        try {
            // Bước 2: Tạo đường dẫn thư mục lưu trữ nếu chưa tồn tại
            Path uploadPath = Paths.get(UPLOAD_DIR, subFolder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Bước 3: Tạo tên file duy nhất bằng UUID để tránh trùng lặp
            String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String extension = getFileExtension(originalFileName);
            String uniqueFileName = UUID.randomUUID().toString() + extension;

            // Bước 4: Lưu file vào ổ đĩa
            Path targetLocation = uploadPath.resolve(uniqueFileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            // Bước 5: Trả về fileKey tương đối (dùng để lưu vào database)
            return subFolder + "/" + uniqueFileName;
        } catch (IOException e) {
            throw new AppException(ErrorCode.INVALID_FILE, "Không thể lưu tệp tin: " + e.getMessage());
        }
    }

    /**
     * Bóc tách fileKey tương đối từ đường dẫn URL hoặc fileKey.
     * Ví dụ: "/api/uploads/branches/uuid.jpg" -> "branches/uuid.jpg"
     *        "uploads/branches/uuid.jpg" -> "branches/uuid.jpg"
     *        "branches/uuid.jpg" -> "branches/uuid.jpg"
     */
    public static String extractFileKey(String urlOrKey) {
        if (urlOrKey == null || urlOrKey.trim().isEmpty()) {
            return null;
        }
        String cleaned = urlOrKey.trim();
        if (cleaned.startsWith("/api/uploads/")) {
            return cleaned.substring("/api/uploads/".length());
        }
        if (cleaned.startsWith("uploads/")) {
            return cleaned.substring("uploads/".length());
        }
        return cleaned;
    }

    /**
     * Xóa file khỏi ổ đĩa theo fileKey hoặc đường dẫn URL.
     *
     * @param fileKeyOrUrl Khóa file tương đối (ví dụ: "branches/uuid.jpg") hoặc URL ("/api/uploads/branches/uuid.jpg")
     * @return true nếu xóa thành công hoặc file không tồn tại, ngược lại false
     */
    public static boolean deleteFile(String fileKeyOrUrl) {
        String fileKey = extractFileKey(fileKeyOrUrl);
        if (fileKey == null || fileKey.isEmpty()) {
            return false;
        }
        try {
            Path filePath = Paths.get(UPLOAD_DIR, fileKey);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Tạo URL xem ảnh đầy đủ từ fileKey hoặc giữ nguyên nếu đã là URL hợp lệ.
     *
     * @param fileKeyOrUrl Khóa file tương đối hoặc đường dẫn URL
     * @return Đường dẫn URL để client truy cập ảnh (ví dụ: "/api/uploads/branches/uuid.jpg")
     */
    public static String buildFileUrl(String fileKeyOrUrl) {
        if (fileKeyOrUrl == null || fileKeyOrUrl.trim().isEmpty()) {
            return null;
        }
        String cleaned = fileKeyOrUrl.trim();
        if (cleaned.startsWith("http://") || cleaned.startsWith("https://") || cleaned.startsWith("/api/uploads/")) {
            return cleaned;
        }
        String fileKey = extractFileKey(cleaned);
        return "/api/uploads/" + fileKey;
    }

    /**
     * Kiểm tra tính hợp lệ của file ảnh (không rỗng, đúng định dạng, không quá dung lượng).
     */
    private static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_FILE, "Tệp tin tải lên không được để trống!");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.INVALID_FILE, "Kích thước tệp tin vượt quá giới hạn 10MB!");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_FILE, "Tên tệp tin không hợp lệ!");
        }

        String extension = getFileExtension(originalFileName).toLowerCase();
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new AppException(ErrorCode.INVALID_FILE,
                    "Định dạng tệp không được hỗ trợ! Chỉ chấp nhận ảnh: JPG, JPEG, PNG, WEBP, GIF.");
        }

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new AppException(ErrorCode.INVALID_FILE,
                    "Loại nội dung tệp không hợp lệ! Chỉ chấp nhận ảnh hợp lệ.");
        }
    }

    /**
     * Lấy phần mở rộng của tên file (bao gồm dấu chấm, ví dụ: ".jpg").
     */
    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            return fileName.substring(dotIndex);
        }
        return "";
    }
}
