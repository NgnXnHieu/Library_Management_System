package com.library.service;

import com.library.dto.inventory.InventoryResponseDto;
import com.library.entity.Book;
import com.library.entity.Branch;
import com.library.entity.Inventory;
import com.library.enums.BranchStatus;
import com.library.enums.DisplayStatus;
import com.library.exception.AppException;
import com.library.exception.BadRequestException;
import com.library.exception.ErrorCode;
import com.library.mapper.InventoryMapper;
import com.library.repository.BookRepository;
import com.library.repository.BranchRepository;
import com.library.repository.InventoryRepository;
import com.library.requestform.inventory.InventoryFilterRequestForm;
import com.library.requestform.inventory.InventoryUpdateRequestForm;
import com.library.security.UserDetailCustom;
import com.library.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Branch branch;
    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        branch = Branch.builder()
                .code("CN01")
                .name("Chi nhánh Hà Nội")
                .status(BranchStatus.OPEN)
                .build();
        branch.setId(1L);

        book1 = Book.builder()
                .title("Sách 1")
                .isbn("ISBN-001")
                .status(DisplayStatus.UNHIDE)
                .build();
        book1.setId(101L);

        book2 = Book.builder()
                .title("Sách 2")
                .isbn("ISBN-002")
                .price(new BigDecimal("120000.00"))
                .rentalPrice(new BigDecimal("12000.00"))
                .fineAmount(new BigDecimal("5000.00"))
                .status(DisplayStatus.UNHIDE)
                .build();
        book2.setId(102L);
    }

    @Test
    @DisplayName("Ném lỗi BRANCH_NOT_FOUND khi chi nhánh không tồn tại")
    void testInitInventories_BranchNotFound_ThrowsException() {
        when(branchRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () ->
                inventoryService.initInventoriesForBranch(99L));

        assertEquals(ErrorCode.BRANCH_NOT_FOUND, exception.getErrorCode());
        verify(inventoryRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Trả về danh sách rỗng khi hệ thống chưa có sách nào")
    void testInitInventories_NoBooksInSystem_ReturnsEmptyList() {
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(bookRepository.findAllWithCategory()).thenReturn(Collections.emptyList());

        List<InventoryResponseDto> result = inventoryService.initInventoriesForBranch(1L);

        assertTrue(result.isEmpty());
        verify(inventoryRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Trả về danh sách rỗng khi tất cả sách đã có sẵn trong kho chi nhánh")
    void testInitInventories_AllBooksAlreadyExist_ReturnsEmptyList() {
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(bookRepository.findAllWithCategory()).thenReturn(List.of(book1, book2));
        when(inventoryRepository.findBookIdsByBranchId(1L)).thenReturn(List.of(101L, 102L));

        List<InventoryResponseDto> result = inventoryService.initInventoriesForBranch(1L);

        assertTrue(result.isEmpty());
        verify(inventoryRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Khởi tạo thành công tồn kho cho những sách chưa có trong kho chi nhánh")
    void testInitInventories_Success() {
        // Giả sử book1 đã có trong kho, chỉ cần khởi tạo thêm book2
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(bookRepository.findAllWithCategory()).thenReturn(List.of(book1, book2));
        when(inventoryRepository.findBookIdsByBranchId(1L)).thenReturn(List.of(101L));

        when(inventoryRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponseDto expectedDto = InventoryResponseDto.builder()
                .branchId(1L)
                .bookId(102L)
                .price(new BigDecimal("120000.00"))
                .rentalPrice(new BigDecimal("12000.00"))
                .fineAmount(new BigDecimal("5000.00"))
                .totalQuantity(0)
                .availableQuantity(0)
                .status(DisplayStatus.UNHIDE)
                .build();
        when(inventoryMapper.toDtoList(any())).thenReturn(List.of(expectedDto));

        List<InventoryResponseDto> result = inventoryService.initInventoriesForBranch(1L);

        assertNotNull(result);
        assertEquals(1, result.size());

        // Kiểm tra đối tượng Inventory được lưu có đúng giá trị mặc định không
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Inventory>> captor = ArgumentCaptor.forClass(List.class);
        verify(inventoryRepository).saveAll(captor.capture());

        List<Inventory> savedList = captor.getValue();
        assertEquals(1, savedList.size());
        Inventory createdInventory = savedList.get(0);
        assertEquals(1L, createdInventory.getBranch().getId());
        assertEquals(102L, createdInventory.getBook().getId());
        assertEquals(0, createdInventory.getTotalQuantity());
        assertEquals(0, createdInventory.getAvailableQuantity());
        assertEquals(DisplayStatus.UNHIDE, createdInventory.getStatus());
        assertNull(createdInventory.getShelfLocation());
    }

    @Test
    @DisplayName("Lấy danh sách phân trang tồn kho thành công kèm bộ lọc và phân trang")
    @SuppressWarnings("unchecked")
    void testGetAllInventories_Success() {
        InventoryFilterRequestForm filter = InventoryFilterRequestForm.builder()
                .branchId(1L)
                .bookTitle("Sách")
                .page(0)
                .size(10)
                .sortBy("id")
                .sortDir("desc")
                .build();

        InventoryResponseDto expectedDto = InventoryResponseDto.builder()
                .id(1L)
                .branchId(1L)
                .bookId(101L)
                .bookTitle("Sách 1")
                .totalQuantity(10)
                .availableQuantity(8)
                .status(DisplayStatus.UNHIDE)
                .build();

        Page<InventoryResponseDto> mockPage = new PageImpl<>(List.of(expectedDto));
        when(inventoryRepository.findAllInventoriesNative(any(), any())).thenReturn(mockPage);

        Page<InventoryResponseDto> result = inventoryService.getAllInventories(filter);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Sách 1", result.getContent().get(0).getBookTitle());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Ném lỗi BadRequestException khi tài khoản nhân viên chưa được gán chi nhánh")
    void testGetInventoriesByCurrentStaffBranch_NoBranchAssigned_ThrowsException() {
        // Giả lập tài khoản đã đăng nhập nhưng branchId = null
        Authentication auth = mock(Authentication.class);
        UserDetailCustom userDetails = UserDetailCustom.builder()
                .userId(10L)
                .branchId(null)
                .build();
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(userDetails);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        InventoryFilterRequestForm filter = new InventoryFilterRequestForm();

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                inventoryService.getInventoriesByCurrentStaffBranch(filter));

        assertEquals("Tài khoản nhân viên chưa được gán chi nhánh hoạt động!", exception.getMessage());
    }

    @Test
    @DisplayName("Lấy tồn kho theo chi nhánh của nhân viên thành công với branchId tự động gán")
    @SuppressWarnings("unchecked")
    void testGetInventoriesByCurrentStaffBranch_Success() {
        // Giả lập nhân viên thuộc chi nhánh ID 1
        Authentication auth = mock(Authentication.class);
        UserDetailCustom userDetails = UserDetailCustom.builder()
                .userId(10L)
                .branchId(1L)
                .build();
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(userDetails);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        InventoryFilterRequestForm filter = new InventoryFilterRequestForm();

        InventoryResponseDto expectedDto = InventoryResponseDto.builder()
                .id(1L)
                .branchId(1L)
                .bookId(101L)
                .bookTitle("Sách 1")
                .totalQuantity(5)
                .availableQuantity(5)
                .build();

        Page<InventoryResponseDto> mockPage = new PageImpl<>(List.of(expectedDto));
        when(inventoryRepository.findAllInventoriesNative(any(), any())).thenReturn(mockPage);

        Page<InventoryResponseDto> result = inventoryService.getInventoriesByCurrentStaffBranch(filter);

        assertNotNull(result);
        assertEquals(1L, filter.getBranchId()); // Kiểm tra branchId đã được tự động gán vào filter
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Ném lỗi BadRequestException khi cập nhật tồn kho sang UNHIDE nhưng sách đang là HIDE")
    void testUpdateInventory_UnhideWithHiddenBook_ThrowsBadRequestException() {
        book1.setStatus(DisplayStatus.HIDE);
        Inventory inventory = Inventory.builder()
                .branch(branch)
                .book(book1)
                .status(DisplayStatus.HIDE)
                .build();
        inventory.setId(5L);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(inventory));

        InventoryUpdateRequestForm form = new InventoryUpdateRequestForm();
        form.setStatus(DisplayStatus.UNHIDE);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                inventoryService.updateInventory(5L, form));

        assertTrue(ex.getMessage().contains("vì đầu sách 'Sách 1' đang ở trạng thái ẩn (HIDE)"));
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ném lỗi BadRequestException khi cập nhật tồn kho sang UNHIDE nhưng chi nhánh đang là CLOSED")
    void testUpdateInventory_UnhideWithClosedBranch_ThrowsBadRequestException() {
        branch.setStatus(BranchStatus.CLOSED);
        Inventory inventory = Inventory.builder()
                .branch(branch)
                .book(book1)
                .status(DisplayStatus.HIDE)
                .build();
        inventory.setId(5L);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(inventory));

        InventoryUpdateRequestForm form = new InventoryUpdateRequestForm();
        form.setStatus(DisplayStatus.UNHIDE);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                inventoryService.updateInventory(5L, form));

        assertTrue(ex.getMessage().contains("vì chi nhánh 'Chi nhánh Hà Nội' đang đóng cửa (CLOSED)"));
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ném lỗi BadRequestException khi đổi trạng thái tồn kho sang UNHIDE nhưng sách đang là HIDE")
    void testChangeInventoryStatus_UnhideWithHiddenBook_ThrowsBadRequestException() {
        book1.setStatus(DisplayStatus.HIDE);
        Inventory inventory = Inventory.builder()
                .branch(branch)
                .book(book1)
                .status(DisplayStatus.HIDE)
                .build();
        inventory.setId(5L);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(inventory));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                inventoryService.changeInventoryStatus(5L, DisplayStatus.UNHIDE));

        assertTrue(ex.getMessage().contains("vì đầu sách 'Sách 1' đang ở trạng thái ẩn (HIDE)"));
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ném lỗi BadRequestException khi đổi trạng thái tồn kho sang UNHIDE nhưng chi nhánh CLOSED")
    void testChangeInventoryStatus_UnhideWithClosedBranch_ThrowsBadRequestException() {
        branch.setStatus(BranchStatus.CLOSED);
        Inventory inventory = Inventory.builder()
                .branch(branch)
                .book(book1)
                .status(DisplayStatus.HIDE)
                .build();
        inventory.setId(5L);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(inventory));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                inventoryService.changeInventoryStatus(5L, DisplayStatus.UNHIDE));

        assertTrue(ex.getMessage().contains("vì chi nhánh 'Chi nhánh Hà Nội' đang đóng cửa (CLOSED)"));
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đổi trạng thái tồn kho sang UNHIDE thành công khi cả book và branch đều hợp lệ")
    void testChangeInventoryStatus_Success() {
        Inventory inventory = Inventory.builder()
                .branch(branch)
                .book(book1)
                .status(DisplayStatus.HIDE)
                .build();
        inventory.setId(5L);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponseDto expectedDto = InventoryResponseDto.builder()
                .id(5L)
                .status(DisplayStatus.UNHIDE)
                .build();
        when(inventoryMapper.toDto(any(Inventory.class))).thenReturn(expectedDto);

        InventoryResponseDto result = inventoryService.changeInventoryStatus(5L, DisplayStatus.UNHIDE);

        assertNotNull(result);
        assertEquals(DisplayStatus.UNHIDE, inventory.getStatus());
        verify(inventoryRepository).save(inventory);
    }
}
