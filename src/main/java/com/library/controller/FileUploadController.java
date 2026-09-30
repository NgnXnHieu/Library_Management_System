package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.file.FileUploadResponseDto;
import com.library.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller tiếp nhận và xử lý việc tải lên tệp tin và hình ảnh.
 * Yêu cầu đăng nhập (xác thực), cho phép mọi vai trò đã đăng nhập truy cập (không cần @PreAuthorize).
 */
@RestController
@RequiredArgsConstructor
public class FileUploadController {

    /**
     * API tải lên hình ảnh lên hệ thống (Yêu cầu đăng nhập).
     *
     * @param file      Tệp tin ảnh (JPG, PNG, WEBP, GIF tối đa 10MB)
     * @param subFolder Thư mục con lưu trữ (mặc định là "books")
     * @return DTO chứa fileKey và fileUrl bọc trong ApiResponse với HTTP 201
     */
    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponseDto>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subFolder", defaultValue = "books") String subFolder) {

        // Bước 1: Lưu file vào ổ đĩa thông qua tiện ích FileUtil
        String fileKey = FileUtil.saveFile(file, subFolder);
        String fileUrl = FileUtil.buildFileUrl(fileKey);

        // Bước 2: Tạo đối tượng DTO phản hồi
        FileUploadResponseDto responseDto = FileUploadResponseDto.builder()
                .originalFileName(file.getOriginalFilename())
                .fileKey(fileKey)
                .fileUrl(fileUrl)
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tải lên ảnh thành công!", responseDto));
    }
}
