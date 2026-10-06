package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.branch.BranchResponseDto;
import com.library.dto.branch.BranchStatisticResponseDto;
import com.library.enums.BranchStatus;
import com.library.requestform.branch.BranchCreateRequestForm;
import com.library.requestform.branch.BranchFilterRequestForm;
import com.library.requestform.branch.BranchUpdateRequestForm;
import com.library.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller tiếp nhận và xử lý các yêu cầu liên quan đến chi nhánh (Branch).
 */
@RestController
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    /**
     * API thêm mới chi nhánh (Yêu cầu quyền ADMIN).
     *
     * @param form Dữ liệu tạo chi nhánh: code, name, address, status (bắt buộc, BranchStatus: OPEN, CLOSED) và phone (tùy chọn)
     * @return DTO chi nhánh vừa tạo bọc trong chuẩn ApiResponse với HTTP 201
     */
    @PostMapping("/branches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponseDto>> createBranch(
            @Valid @RequestBody BranchCreateRequestForm form) {
        BranchResponseDto createdBranch = branchService.createBranch(form);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm mới chi nhánh thành công!", createdBranch));
    }

    /**
     * API cập nhật thông tin chi nhánh (Yêu cầu quyền ADMIN).
     *
     * @param id ID của chi nhánh cần cập nhật
     * @param form Dữ liệu cập nhật
     * @return DTO chi nhánh sau khi cập nhật
     */
    @PutMapping("/branches/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BranchResponseDto>> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchUpdateRequestForm form) {
        BranchResponseDto updatedBranch = branchService.updateBranch(id, form);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chi nhánh thành công!", updatedBranch));
    }

    /**
     * API xóa chi nhánh (Yêu cầu quyền ADMIN).
     *
     * @param id ID của chi nhánh cần xóa
     * @return Phản hồi thành công
     */
    @DeleteMapping("/branches/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa chi nhánh thành công!", null));
    }

    /**
     * API lấy thông tin chi tiết chi nhánh theo ID (Cho phép mọi tài khoản đã đăng nhập).
     *
     * @param id ID của chi nhánh
     * @return DTO chi tiết chi nhánh
     */
    @GetMapping("/branches/{id}")
    public ResponseEntity<ApiResponse<BranchResponseDto>> getBranchById(@PathVariable Long id) {
        BranchResponseDto branch = branchService.getBranchById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi nhánh thành công!", branch));
    }

    /**
     * API lấy danh sách toàn bộ các chi nhánh (Cho phép mọi tài khoản đã đăng nhập).
     *
     * @return Danh sách chi nhánh
     */
    @GetMapping("/branches")
    public ResponseEntity<ApiResponse<List<BranchResponseDto>>> getAllBranches() {
        List<BranchResponseDto> branches = branchService.getAllBranches();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chi nhánh thành công!", branches));
    }

    /**
     * API công khai lấy danh sách chi nhánh (Không cần đăng nhập, tiền tố /public).
     *
     * @return Danh sách chi nhánh
     */
    @GetMapping("/public/branches")
    public ResponseEntity<ApiResponse<List<BranchResponseDto>>> getPublicBranches() {
        List<BranchResponseDto> branches = branchService.getAllBranches();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chi nhánh thành công!", branches));
    }

    /**
     * API lấy danh sách phân trang các chi nhánh kèm bộ lọc tìm kiếm và sắp xếp (Dành riêng cho ADMIN).
     * Mặc định sắp xếp theo createdAt với thời gian mới nhất lên đầu (DESC).
     *
     * @param filter Bộ lọc tìm kiếm (code, name, address, phone, status) và tham số phân trang nhận qua Query Parameters
     * @return Trang kết quả chứa danh sách DTO chi nhánh bọc trong chuẩn ApiResponse
     */
    @GetMapping("/admin/branches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<BranchResponseDto>>> getBranchesPage(
            @Valid @ModelAttribute BranchFilterRequestForm filter) {
        // Bước 1: Gọi tầng Service xử lý truy vấn phân trang qua Specification
        Page<BranchResponseDto> result = branchService.getBranchesWithFilter(filter);

        // Bước 2: Bọc kết quả trả về trong chuẩn ApiResponse với HTTP 200 OK
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chi nhánh phân trang thành công!", result));
    }

    /**
     * API công khai lấy danh sách toàn bộ các giá trị trạng thái hoạt động của chi nhánh (Không cần đăng nhập, tiền tố /public).
     *
     * @return Danh sách các enum BranchStatus (OPEN, CLOSED) bọc trong chuẩn ApiResponse
     */
    @GetMapping("/public/branches/statuses")
    public ResponseEntity<ApiResponse<List<BranchStatus>>> getBranchStatuses() {
        // Bước 1: Gọi tầng Service lấy danh sách các trạng thái chi nhánh
        List<BranchStatus> statuses = branchService.getBranchStatuses();

        // Bước 2: Bọc kết quả trả về trong chuẩn ApiResponse với HTTP 200 OK
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách trạng thái chi nhánh thành công!", statuses));
    }

    /**
     * API lấy danh sách phân trang thống kê các chi nhánh thư viện (Dành riêng cho ADMIN).
     * Trả về thông tin chi nhánh kèm tổng số sách trong kho, số sách còn, số sách đang mượn, lượt mượn và doanh thu.
     *
     * @param filter Bộ lọc tìm kiếm (code, name, status) và tham số phân trang nhận qua Query Parameters
     * @return Trang kết quả chứa danh sách BranchStatisticResponseDto bọc trong chuẩn ApiResponse
     */
    @GetMapping("/admin/branches/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<BranchStatisticResponseDto>>> getBranchStatistics(
            @Valid @ModelAttribute BranchFilterRequestForm filter) {
        // Bước 1: Gọi tầng Service xử lý truy vấn phân trang thống kê chi nhánh
        Page<BranchStatisticResponseDto> result = branchService.getBranchStatistics(filter);

        // Bước 2: Bọc kết quả trả về trong chuẩn ApiResponse với HTTP 200 OK
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thống kê chi nhánh thành công!", result));
    }

    /**
     * API xuất báo cáo thống kê toàn bộ chi nhánh ra file Excel (Dành riêng cho ADMIN).
     * Xuất tệp tin .xlsx theo mẫu JasperReports với đầy đủ các chỉ số vĩ mô và bảng chi tiết từng chi nhánh.
     *
     * @param filter Bộ lọc tìm kiếm (code, name, status, fromDate, toDate) nhận qua Query Parameters
     * @return Tệp tin Excel dạng mảng byte kèm Content-Disposition attachment để tải về
     */
    @GetMapping(value = "/admin/branches/statistics/export", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportBranchStatistics(
            @Valid @ModelAttribute BranchFilterRequestForm filter) {
        // Bước 1: Gọi tầng Service xuất dữ liệu báo cáo thống kê ra mảng byte file Excel
        byte[] excelBytes = branchService.exportBranchStatisticsExcel(filter);

        // Bước 2: Thiết lập tên tệp tin động kèm dấu thời gian
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "Bao_cao_tong_hop_chi_nhanh_" + timestamp + ".xlsx";

        // Bước 3: Trả về file Excel kèm các Header phù hợp để trình duyệt tự động tải xuống
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
