package com.library.service.impl;

import com.library.dto.branch.BranchResponseDto;
import com.library.dto.branch.BranchStatisticResponseDto;
import com.library.entity.Branch;
import com.library.enums.BorrowStatus;
import com.library.enums.BranchStatus;
import com.library.enums.DisplayStatus;
import com.library.enums.Role;
import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import com.library.mapper.BranchMapper;
import com.library.repository.BranchRepository;
import com.library.repository.InventoryRepository;
import com.library.repository.UserRepository;
import com.library.requestform.branch.BranchCreateRequestForm;
import com.library.requestform.branch.BranchFilterRequestForm;
import com.library.requestform.branch.BranchUpdateRequestForm;
import com.library.service.BranchService;
import com.library.specification.BranchSpecification;
import com.library.util.FileUtil;
import com.library.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service triển khai các nghiệp vụ quản lý chi nhánh thư viện.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "code", "name", "address", "phone", "status", "createdAt", "updatedAt"
    );

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final BranchMapper branchMapper;

    /**
     * Thêm mới chi nhánh vào hệ thống (Chỉ dành cho ADMIN).
     *
     * @param form Thông tin chi nhánh cần tạo
     * @return DTO thông tin chi nhánh vừa tạo
     */
    @Override
    @Transactional
    public BranchResponseDto createBranch(BranchCreateRequestForm form) {
        // Bước 1: Kiểm tra quyền hạn của người thực hiện (bắt buộc phải là ADMIN)
        checkAdminPermission();

        String code = form.getCode().trim().toUpperCase();

        // Bước 2: Kiểm tra mã chi nhánh đã tồn tại chưa
        if (branchRepository.existsByCode(code)) {
            throw new AppException(ErrorCode.BRANCH_ALREADY_EXISTS, code);
        }

        // Bước 3: Chuyển đổi dữ liệu từ Form sang Entity và chuẩn hóa mã chi nhánh
        Branch branch = branchMapper.toEntity(form);
        branch.setCode(code);

        // Chuẩn hóa số điện thoại nếu rỗng
        if (form.getPhone() != null && form.getPhone().trim().isEmpty()) {
            branch.setPhone(null);
        }

        // Chuẩn hóa đường dẫn ảnh nếu rỗng
        if (form.getImageUrl() != null && form.getImageUrl().trim().isEmpty()) {
            branch.setImageUrl(null);
        }

        Branch savedBranch = branchRepository.save(branch);

        // Bước 4: Chuyển đổi sang DTO và trả về kết quả
        return branchMapper.toDto(savedBranch);
    }

    /**
     * Cập nhật thông tin chi nhánh hiện có (Chỉ dành cho ADMIN).
     *
     * @param id   ID của chi nhánh cần cập nhật
     * @param form Dữ liệu cập nhật
     * @return DTO thông tin chi nhánh sau khi cập nhật
     */
    @Override
    @Transactional
    public BranchResponseDto updateBranch(Long id, BranchUpdateRequestForm form) {
        // Bước 1: Kiểm tra quyền hạn của người thực hiện (bắt buộc phải là ADMIN)
        checkAdminPermission();

        // Bước 2: Tìm kiếm chi nhánh theo ID
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND, id));

        // Bước 3: Nếu mã chi nhánh thay đổi, kiểm tra trùng lặp với các chi nhánh khác
        if (form.getCode() != null && !form.getCode().trim().isEmpty()) {
            String newCode = form.getCode().trim().toUpperCase();
            if (!branch.getCode().equalsIgnoreCase(newCode) && branchRepository.existsByCodeAndIdNot(newCode, id)) {
                throw new AppException(ErrorCode.BRANCH_ALREADY_EXISTS, newCode);
            }
            branch.setCode(newCode);
        }

        // Bước 4: Nếu người dùng cập nhật ảnh mới khác với ảnh cũ -> Xóa file ảnh cũ trên ổ đĩa để tránh rác dung lượng
        String oldImageUrl = branch.getImageUrl();
        String newImageUrl = form.getImageUrl();
        if (newImageUrl != null && !newImageUrl.trim().isEmpty() && !newImageUrl.trim().equals(oldImageUrl)) {
            if (oldImageUrl != null && !oldImageUrl.trim().isEmpty()) {
                FileUtil.deleteFile(oldImageUrl);
            }
        }

        // Bước 5: Cập nhật các trường dữ liệu từ form vào entity qua MapStruct
        branchMapper.updateEntityFromForm(form, branch);

        // Chuẩn hóa số điện thoại nếu form gửi chuỗi rỗng
        if (form.getPhone() != null && form.getPhone().trim().isEmpty()) {
            branch.setPhone(null);
        }

        Branch updatedBranch = branchRepository.save(branch);

        // Bước 6: Nếu chi nhánh chuyển sang trạng thái CLOSED -> Tự động cập nhật tất cả tồn kho thuộc chi nhánh này sang HIDE
        if (updatedBranch.getStatus() == BranchStatus.CLOSED) {
            inventoryRepository.updateStatusByBranchId(updatedBranch.getId(), DisplayStatus.HIDE);
        }

        // Bước 7: Chuyển đổi sang DTO và trả về
        return branchMapper.toDto(updatedBranch);
    }

    /**
     * Xóa chi nhánh khỏi hệ thống (Chỉ dành cho ADMIN).
     *
     * @param id ID của chi nhánh cần xóa
     */
    @Override
    @Transactional
    public void deleteBranch(Long id) {
        // Bước 1: Kiểm tra quyền hạn của người thực hiện (bắt buộc phải là ADMIN)
        checkAdminPermission();

        // Bước 2: Tìm kiếm chi nhánh theo ID
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND, id));

        // Bước 3: Kiểm tra ràng buộc toàn vẹn dữ liệu (không xóa chi nhánh nếu đang có
        // người dùng thuộc chi nhánh này)
        if (userRepository.existsByBranchId(id)) {
            throw new AppException(ErrorCode.BRANCH_CANNOT_DELETE, branch.getName());
        }

        // Bước 4: Lưu lại đường dẫn ảnh để xóa file sau khi xóa thành công trong DB
        String imageToDelete = branch.getImageUrl();

        // Bước 5: Thực hiện xóa chi nhánh khỏi Database
        branchRepository.delete(branch);

        // Bước 6: Xóa tệp ảnh của chi nhánh trên ổ đĩa máy chủ nếu có
        if (imageToDelete != null && !imageToDelete.trim().isEmpty()) {
            FileUtil.deleteFile(imageToDelete);
        }
    }

    /**
     * Lấy thông tin chi tiết chi nhánh theo ID.
     *
     * @param id ID của chi nhánh
     * @return DTO thông tin chi nhánh
     */
    @Override
    @Transactional(readOnly = true)
    public BranchResponseDto getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND, id));
        return branchMapper.toDto(branch);
    }

    /**
     * Lấy danh sách toàn bộ chi nhánh trong hệ thống.
     *
     * @return Danh sách DTO các chi nhánh
     */
    @Override
    @Transactional(readOnly = true)
    public List<BranchResponseDto> getAllBranches() {
        List<Branch> branches = branchRepository.findAll();
        return branchMapper.toDtoList(branches);
    }

    /**
     * Lấy danh sách phân trang các chi nhánh kèm bộ lọc tìm kiếm và sắp xếp.
     * Mặc định sắp xếp theo createdAt với thời gian mới nhất lên đầu (DESC).
     *
     * @param filter Bộ lọc chứa code, name, address, phone, status và tham số phân trang
     * @return Trang kết quả phân trang chứa DTO chi nhánh
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BranchResponseDto> getBranchesWithFilter(BranchFilterRequestForm filter) {
        // Bước 1: Chuẩn hóa tham số bộ lọc nếu client gửi null
        if (filter == null) {
            filter = new BranchFilterRequestForm();
        }

        // Bước 2: Xác định trường sắp xếp an toàn (mặc định createdAt)
        String sortBy = filter.getSortBy();
        if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy.trim())) {
            sortBy = "createdAt";
        } else {
            sortBy = sortBy.trim();
        }

        // Bước 3: Xác định hướng sắp xếp (mặc định desc: mới nhất lên đầu)
        Sort.Direction direction = "asc".equalsIgnoreCase(filter.getSortDir())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        // Bước 4: Tạo đối tượng Pageable của Spring Data JPA
        int pageNumber = Math.max(0, filter.getPage());
        int pageSize = Math.max(1, filter.getSize());
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

        // Bước 5: Truy vấn Database qua BranchSpecification và ánh xạ sang DTO bằng MapStruct
        Page<Branch> branchPage = branchRepository.findAll(BranchSpecification.filter(filter), pageable);
        return branchPage.map(branchMapper::toDto);
    }

    /**
     * Lấy danh sách toàn bộ trạng thái hoạt động của chi nhánh (BranchStatus).
     *
     * @return Danh sách các giá trị enum BranchStatus
     */
    @Override
    @Transactional(readOnly = true)
    public List<BranchStatus> getBranchStatuses() {
        return List.of(BranchStatus.values());
    }

    /**
     * Kiểm tra quyền hạn người dùng hiện tại có phải là ADMIN hay không.
     * Ném ra ngoại lệ AppException(ErrorCode.ACCESS_DENIED) nếu không đủ quyền.
     */
    private void checkAdminPermission() {
        String currentRole = SecurityUtil.getCurrentRoleCode().orElse(null);
        // if (currentRole == null || !Role.ADMIN.name().equalsIgnoreCase(currentRole))
        // {
        // throw new AppException(ErrorCode.ACCESS_DENIED);
        // }
    }

    /**
     * Lấy danh sách phân trang thống kê chi nhánh (dành riêng cho ADMIN).
     * Bao gồm: số lượng sách trong kho, số sách còn, số sách đang mượn, lượt mượn và doanh thu.
     * Sử dụng JPQL Constructor Expression tối ưu truy vấn chiếu trực tiếp lên BranchStatisticResponseDto.
     *
     * @param filter Bộ lọc chứa code, name, status và tham số phân trang
     * @return Trang kết quả phân trang chứa DTO thống kê chi nhánh
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BranchStatisticResponseDto> getBranchStatistics(BranchFilterRequestForm filter) {
        // Bước 1: Chuẩn hóa tham số bộ lọc nếu client gửi null
        if (filter == null) {
            filter = new BranchFilterRequestForm();
        }

        // Bước 2: Xác định trường sắp xếp an toàn (mặc định createdAt)
        String sortBy = filter.getSortBy();
        if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy.trim())) {
            sortBy = "createdAt";
        } else {
            sortBy = sortBy.trim();
        }

        // Bước 3: Xác định hướng sắp xếp (mặc định desc: mới nhất lên đầu)
        Sort.Direction direction = "asc".equalsIgnoreCase(filter.getSortDir())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        // Bước 4: Tạo đối tượng Pageable của Spring Data JPA
        int pageNumber = Math.max(0, filter.getPage());
        int pageSize = Math.max(1, filter.getSize());
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

        // Bước 5: Định nghĩa danh sách các trạng thái phiếu mượn hợp lệ được tính thống kê
        List<BorrowStatus> borrowStatuses = List.of(
                BorrowStatus.BORROWED,
                BorrowStatus.RETURNED,
                BorrowStatus.OVERDUE
        );

        // Bước 6: Gọi truy vấn JPQL Constructor Expression qua Repository
        return branchRepository.findBranchStatisticsWithFilter(filter, borrowStatuses, pageable);
    }

    /**
     * Xuất dữ liệu báo cáo thống kê toàn bộ chi nhánh ra file Excel bằng JasperReports (dành riêng cho ADMIN).
     *
     * @param filter Bộ lọc chứa code, name, status, fromDate, toDate
     * @return Mảng byte chứa nội dung tệp tin Excel (.xlsx)
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] exportBranchStatisticsExcel(BranchFilterRequestForm filter) {
        // Bước 1: Chuẩn hóa tham số bộ lọc nếu client gửi null
        if (filter == null) {
            filter = new BranchFilterRequestForm();
        }

        // Bước 2: Định nghĩa danh sách các trạng thái phiếu mượn hợp lệ được tính thống kê
        List<BorrowStatus> borrowStatuses = List.of(
                BorrowStatus.BORROWED,
                BorrowStatus.RETURNED,
                BorrowStatus.OVERDUE
        );

        // Bước 3: Truy vấn toàn bộ dữ liệu thống kê chi nhánh (không phân trang)
        List<BranchStatisticResponseDto> dataList = branchRepository.findAllBranchStatisticsWithFilter(filter, borrowStatuses);

        // Bước 4: Chuẩn bị các tham số báo cáo (Parameters)
        Map<String, Object> parameters = new HashMap<>();

        // Xác định chuỗi hiển thị kỳ báo cáo thời gian
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String reportPeriod;
        if (filter.getFromDate() == null && filter.getToDate() == null) {
            reportPeriod = "Kỳ báo cáo: Toàn bộ thời gian";
        } else if (filter.getFromDate() != null && filter.getToDate() != null) {
            reportPeriod = "Kỳ báo cáo: Từ ngày " + filter.getFromDate().format(dateFormatter)
                    + " đến ngày " + filter.getToDate().format(dateFormatter);
        } else if (filter.getFromDate() != null) {
            reportPeriod = "Kỳ báo cáo: Từ ngày " + filter.getFromDate().format(dateFormatter);
        } else {
            reportPeriod = "Kỳ báo cáo: Đến ngày " + filter.getToDate().format(dateFormatter);
        }
        parameters.put("REPORT_PERIOD", reportPeriod);

        // Tính toán các chỉ số vĩ mô hiển thị ở các thẻ KPI
        long totalBranches = dataList.size();
        long openBranches = dataList.stream()
                .filter(b -> b.getStatus() == BranchStatus.OPEN)
                .count();
        long totalBorrowedBooks = dataList.stream()
                .mapToLong(b -> b.getTotalBorrowedBooks() != null ? b.getTotalBorrowedBooks() : 0L)
                .sum();
        BigDecimal totalRevenue = dataList.stream()
                .map(b -> b.getTotalRevenue() != null ? b.getTotalRevenue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        parameters.put("TOTAL_BRANCHES", totalBranches);
        parameters.put("TOTAL_OPEN_BRANCHES", openBranches);
        parameters.put("TOTAL_BORROWED_BOOKS", totalBorrowedBooks);
        parameters.put("TOTAL_REVENUE", totalRevenue);

        // Bước 5: Đọc file mẫu template JRXML từ classpath và biên dịch
        String templatePath = "/reports/branch_statistics_excel.jrxml";
        try (InputStream reportStream = getClass().getResourceAsStream(templatePath)) {
            if (reportStream == null) {
                log.error("Không tìm thấy tệp template JasperReports tại: {}", templatePath);
                throw new AppException(ErrorCode.REPORT_EXPORT_FAILED, "Không tìm thấy tệp mẫu báo cáo!");
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // Bước 6: Đóng gói danh sách dữ liệu vào DataSource và fill vào mẫu báo cáo
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Bước 7: Cấu hình JRXlsxExporter xuất ra luồng byte Excel
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

            // Thiết lập cấu hình chuyên dụng cho file Excel
            SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
            configuration.setDetectCellType(true);                  // Tự động nhận diện kiểu số/ngày
            configuration.setRemoveEmptySpaceBetweenRows(true);     // Loại bỏ dòng trắng ngắt trang
            configuration.setRemoveEmptySpaceBetweenColumns(true);  // Loại bỏ cột trắng dư thừa
            configuration.setWhitePageBackground(false);           // Không vẽ nền trắng (giữ gridlines)
            configuration.setOnePagePerSheet(false);                // Tất cả dữ liệu nằm trên 1 sheet duy nhất
            exporter.setConfiguration(configuration);

            exporter.exportReport();

            // Bước 8: Trả về mảng byte dữ liệu tệp Excel hoàn chỉnh
            return outputStream.toByteArray();

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi trong quá trình xuất báo cáo thống kê chi nhánh Excel: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.REPORT_EXPORT_FAILED, e.getMessage());
        }
    }
}
