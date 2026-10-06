package com.library.service;

import com.library.dto.branch.BranchResponseDto;
import com.library.dto.branch.BranchStatisticResponseDto;
import com.library.entity.Branch;
import com.library.enums.BorrowStatus;
import com.library.enums.BranchStatus;
import com.library.enums.DisplayStatus;
import com.library.mapper.BranchMapper;
import com.library.repository.BranchRepository;
import com.library.repository.InventoryRepository;
import com.library.repository.UserRepository;
import com.library.requestform.branch.BranchFilterRequestForm;
import com.library.requestform.branch.BranchUpdateRequestForm;
import com.library.service.impl.BranchServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchServiceImplTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private BranchMapper branchMapper;

    @InjectMocks
    private BranchServiceImpl branchService;

    @Test
    @DisplayName("Kiểm tra phân trang chi nhánh với bộ lọc và sắp xếp mặc định createdAt DESC")
    void testGetBranchesWithFilter_DefaultSortCreatedAtDesc() {
        BranchFilterRequestForm filter = BranchFilterRequestForm.builder()
                .code("CN01")
                .name("Chi nhánh Hà Nội")
                .status(BranchStatus.OPEN)
                .build();

        Branch branch = Branch.builder()
                .code("CN01")
                .name("Chi nhánh Hà Nội")
                .status(BranchStatus.OPEN)
                .build();

        BranchResponseDto dto = BranchResponseDto.builder()
                .code("CN01")
                .name("Chi nhánh Hà Nội")
                .status(BranchStatus.OPEN)
                .build();

        Page<Branch> branchPage = new PageImpl<>(List.of(branch));
        when(branchRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(branchPage);
        when(branchMapper.toDto(branch)).thenReturn(dto);

        Page<BranchResponseDto> result = branchService.getBranchesWithFilter(filter);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("CN01", result.getContent().get(0).getCode());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(branchRepository).findAll(any(Specification.class), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        Sort.Order order = capturedPageable.getSort().getOrderFor("createdAt");
        assertNotNull(order, "Sắp xếp mặc định phải chứa trường createdAt");
        assertTrue(order.isDescending(), "Sắp xếp mặc định theo createdAt phải là DESC");
    }

    @Test
    @DisplayName("Kiểm tra lấy danh sách toàn bộ các giá trị enum BranchStatus")
    void testGetBranchStatuses() {
        List<BranchStatus> statuses = branchService.getBranchStatuses();

        assertNotNull(statuses);
        assertEquals(2, statuses.size());
        assertTrue(statuses.contains(BranchStatus.OPEN));
        assertTrue(statuses.contains(BranchStatus.CLOSED));
    }

    @Test
    @DisplayName("Cập nhật chi nhánh sang trạng thái CLOSED -> Tự động cập nhật tất cả tồn kho sang HIDE")
    void testUpdateBranch_StatusClosed_CascadesToInventory() {
        Branch branch = Branch.builder()
                .code("CN01")
                .name("Chi nhánh Hà Nội")
                .status(BranchStatus.OPEN)
                .build();
        branch.setId(1L);

        BranchUpdateRequestForm form = BranchUpdateRequestForm.builder()
                .status(BranchStatus.CLOSED)
                .build();

        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        doAnswer(invocation -> {
            Branch b = invocation.getArgument(1);
            b.setStatus(BranchStatus.CLOSED);
            return null;
        }).when(branchMapper).updateEntityFromForm(eq(form), any(Branch.class));

        when(branchRepository.save(any(Branch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BranchResponseDto dto = BranchResponseDto.builder()
                .id(1L)
                .code("CN01")
                .status(BranchStatus.CLOSED)
                .build();
        when(branchMapper.toDto(any(Branch.class))).thenReturn(dto);

        BranchResponseDto result = branchService.updateBranch(1L, form);

        assertNotNull(result);
        assertEquals(BranchStatus.CLOSED, branch.getStatus());
        verify(branchRepository).save(branch);
        verify(inventoryRepository).updateStatusByBranchId(1L, DisplayStatus.HIDE);
    }

    @Test
    @DisplayName("Kiểm tra lấy danh sách phân trang thống kê chi nhánh với bộ lọc và tính toán số sách đang mượn")
    void testGetBranchStatistics_Success() {
        BranchFilterRequestForm filter = BranchFilterRequestForm.builder()
                .code("CN01")
                .name("Chi nhánh Hà Nội")
                .status(BranchStatus.OPEN)
                .build();

        BranchStatisticResponseDto statDto = new BranchStatisticResponseDto(
                1L,
                "CN01",
                "Chi nhánh Hà Nội",
                BranchStatus.OPEN,
                "branches/hanoi.jpg",
                100L,
                70L,
                15L,
                new BigDecimal("1500000.00")
        );

        Page<BranchStatisticResponseDto> statPage = new PageImpl<>(List.of(statDto));
        when(branchRepository.findBranchStatisticsWithFilter(eq(filter), any(), any(Pageable.class)))
                .thenReturn(statPage);

        Page<BranchStatisticResponseDto> result = branchService.getBranchStatistics(filter);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        BranchStatisticResponseDto item = result.getContent().get(0);
        assertEquals("CN01", item.getCode());
        assertEquals(100L, item.getTotalBooksInStock());
        assertEquals(70L, item.getTotalAvailableBooks());
        assertEquals(30L, item.getTotalBorrowedBooks()); // 100 - 70 = 30
        assertEquals(15L, item.getTotalBorrowSlips());
        assertEquals(new BigDecimal("1500000.00"), item.getTotalRevenue());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BorrowStatus>> statusesCaptor = ArgumentCaptor.forClass(List.class);
        verify(branchRepository).findBranchStatisticsWithFilter(eq(filter), statusesCaptor.capture(), pageableCaptor.capture());

        List<BorrowStatus> capturedStatuses = statusesCaptor.getValue();
        assertTrue(capturedStatuses.contains(BorrowStatus.BORROWED));
        assertTrue(capturedStatuses.contains(BorrowStatus.RETURNED));
        assertTrue(capturedStatuses.contains(BorrowStatus.OVERDUE));
    }

    @Test
    @DisplayName("Xuất báo cáo thống kê chi nhánh ra Excel thành công khi có dữ liệu")
    void testExportBranchStatisticsExcel_Success() throws Exception {
        // Bước 1: Chuẩn bị dữ liệu giả lập
        BranchFilterRequestForm filter = new BranchFilterRequestForm();
        BranchStatisticResponseDto statDto = new BranchStatisticResponseDto(
                1L,
                "CN01",
                "Chi nhánh Hà Nội",
                BranchStatus.OPEN,
                "branches/hanoi.jpg",
                100L,
                70L,
                15L,
                new BigDecimal("1500000.00")
        );

        when(branchRepository.findAllBranchStatisticsWithFilter(eq(filter), any()))
                .thenReturn(List.of(statDto));

        // Bước 2: Gọi phương thức xuất Excel
        byte[] result = branchService.exportBranchStatisticsExcel(filter);

        // Bước 3: Kiểm tra tính hợp lệ của mảng byte và cấu trúc file Excel
        assertNotNull(result);
        assertTrue(result.length > 0);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertNotNull(sheet);
            assertTrue(sheet.getPhysicalNumberOfRows() >= 8, "Bảng Excel phải chứa tiêu đề, KPI, header và ít nhất 1 dòng dữ liệu");
        }
    }

    @Test
    @DisplayName("Xuất báo cáo thống kê chi nhánh ra Excel thành công khi dữ liệu rỗng (vẫn có khung bảng & KPI)")
    void testExportBranchStatisticsExcel_EmptyData() throws Exception {
        // Bước 1: Giả lập repository trả về danh sách rỗng
        BranchFilterRequestForm filter = new BranchFilterRequestForm();
        when(branchRepository.findAllBranchStatisticsWithFilter(eq(filter), any()))
                .thenReturn(List.of());

        // Bước 2: Gọi phương thức xuất Excel
        byte[] result = branchService.exportBranchStatisticsExcel(filter);

        // Bước 3: Đảm bảo JasperReports xuất ra file có tiêu đề, KPI và Header bảng (không bị 0 dòng)
        assertNotNull(result);
        assertTrue(result.length > 0);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertNotNull(sheet);
            assertTrue(sheet.getPhysicalNumberOfRows() >= 6, "File Excel khi rỗng dữ liệu vẫn phải có phần Tiêu đề, thẻ KPI và dòng Header bảng");
        }
    }
}

