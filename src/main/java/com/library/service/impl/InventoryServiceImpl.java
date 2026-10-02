package com.library.service.impl;

import com.library.dto.inventory.InventoryResponseDto;
import com.library.entity.Book;
import com.library.entity.Branch;
import com.library.entity.Inventory;
import com.library.enums.DisplayStatus;
import com.library.exception.AppException;
import com.library.exception.BadRequestException;
import com.library.exception.ErrorCode;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.InventoryMapper;
import com.library.repository.BookRepository;
import com.library.repository.BranchRepository;
import com.library.repository.InventoryRepository;
import com.library.requestform.inventory.InventoryFilterRequestForm;
import com.library.requestform.inventory.InventoryImportRequestForm;
import com.library.requestform.inventory.InventoryUpdateRequestForm;
import com.library.service.InventoryService;
import com.library.specification.InventorySpecification;
import com.library.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service triển khai các nghiệp vụ quản lý tồn kho sách tại chi nhánh.
 */
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final BranchRepository branchRepository;
    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    /**
     * Tự động khởi tạo tồn kho cho tất cả các cuốn sách đang có trong hệ thống tại một chi nhánh.
     * Chỉ tạo mới cho các cuốn sách chưa có trong kho của chi nhánh đó (tránh trùng lặp).
     *
     * @param branchId ID chi nhánh cần khởi tạo tồn kho
     * @return Danh sách DTO các bản ghi tồn kho vừa được tạo mới
     */
    @Override
    @Transactional
    public List<InventoryResponseDto> initInventoriesForBranch(Long branchId) {
        // Bước 1: Kiểm tra chi nhánh có tồn tại trong hệ thống hay không
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND, branchId));

        // Bước 2: Lấy tất cả các cuốn sách đang có trong hệ thống (kèm Category bằng JOIN FETCH để tránh N+1 query)
        List<Book> allBooks = bookRepository.findAllWithCategory();
        if (allBooks.isEmpty()) {
            return Collections.emptyList();
        }

        // Bước 3: Lấy danh sách ID các cuốn sách đã có trong kho của chi nhánh này
        List<Long> existingBookIds = inventoryRepository.findBookIdsByBranchId(branchId);
        Set<Long> existingBookIdSet = new HashSet<>(existingBookIds);

        // Bước 4: Lọc ra các cuốn sách chưa có trong kho của chi nhánh
        List<Book> booksToInit = allBooks.stream()
                .filter(book -> !existingBookIdSet.contains(book.getId()))
                .toList();

        if (booksToInit.isEmpty()) {
            return Collections.emptyList();
        }

        // Bước 5: Khởi tạo danh sách đối tượng Inventory với các giá trị mặc định đã thống nhất
        List<Inventory> newInventories = new ArrayList<>();
        for (Book book : booksToInit) {
            Inventory inventory = Inventory.builder()
                    .branch(branch)
                    .book(book)
                    .totalQuantity(0)
                    .availableQuantity(0)
                    .status(DisplayStatus.UNHIDE)
                    .shelfLocation(null)
                    .build();
            newInventories.add(inventory);
        }

        // Bước 6: Lưu hàng loạt vào Database
        List<Inventory> savedInventories = inventoryRepository.saveAll(newInventories);

        // Bước 7: Chuyển đổi sang danh sách DTO và trả về kết quả
        return inventoryMapper.toDtoList(savedInventories);
    }

    /**
     * Lấy danh sách tồn kho sách phân trang kèm theo bộ lọc tìm kiếm và sắp xếp (Public).
     * Sử dụng JOIN FETCH đa tầng để tối ưu hiệu năng và tránh N+1 query.
     *
     * @param filter Bộ lọc tìm kiếm và phân trang
     * @return Trang kết quả chứa danh sách InventoryResponseDto
     */
    @Override
    @Transactional(readOnly = true)
    public Page<InventoryResponseDto> getAllInventories(InventoryFilterRequestForm filter) {
        // Bước 1: Khởi tạo Specification từ filter form (đã bao gồm JOIN FETCH đa tầng)
        Specification<Inventory> spec = InventorySpecification.filter(filter);

        // Bước 2: Xác định hướng và trường sắp xếp (Sort) - mặc định theo updated_at từ cũ -> mới nhất (ASC)
        Sort.Direction direction = (filter.getSortDir() != null && "desc".equalsIgnoreCase(filter.getSortDir()))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        String rawSortBy = (filter.getSortBy() != null && !filter.getSortBy().trim().isEmpty())
                ? filter.getSortBy().trim()
                : "updatedAt";

        // Ánh xạ các trường sắp xếp sang đường dẫn thuộc tính thực thể tương ứng
        String sortBy = switch (rawSortBy) {
            case "bookTitle" -> "book.title";
            case "price" -> "book.price";
            case "rentalPrice" -> "book.rentalPrice";
            case "fineAmount" -> "book.fineAmount";
            case "categoryName" -> "book.category.name";
            case "branchName" -> "branch.name";
            default -> rawSortBy;
        };

        Sort sort = Sort.by(direction, sortBy);

        // Bước 3: Khởi tạo Pageable
        int page = Math.max(filter.getPage(), 0);
        int size = filter.getSize() > 0 ? filter.getSize() : 10;
        Pageable pageable = PageRequest.of(page, size, sort);

        // Bước 4: Thực hiện truy vấn kết hợp Specification và phân trang
        Page<Inventory> inventoryPage = inventoryRepository.findAll(spec, pageable);

        // Bước 5: Ánh xạ kết quả sang DTO bằng MapStruct
        return inventoryPage.map(inventoryMapper::toDto);
    }

    /**
     * Lấy danh sách phân trang tồn kho theo chi nhánh làm việc của nhân viên đang đăng nhập.
     * Tự động lấy branchId từ SecurityContextHolder và gán vào bộ lọc.
     *
     * @param filter Bộ lọc tìm kiếm và phân trang
     * @return Trang kết quả tồn kho sách của chi nhánh
     */
    @Override
    @Transactional(readOnly = true)
    public Page<InventoryResponseDto> getInventoriesByCurrentStaffBranch(InventoryFilterRequestForm filter) {
        // Bước 1: Trích xuất branchId của nhân viên đang đăng nhập từ SecurityContextHolder
        Long branchId = SecurityUtil.getCurrentBranchId()
                .orElseThrow(() -> new BadRequestException("Tài khoản nhân viên chưa được gán chi nhánh hoạt động!"));

        // Bước 2: Thiết lập chi nhánh bắt buộc cho bộ lọc tìm kiếm (ghi đè mọi branchId client truyền lên)
        filter.setBranchId(branchId);

        // Bước 3: Tái sử dụng logic truy vấn phân trang và tìm kiếm tồn kho đã tối ưu
        return getAllInventories(filter);
    }

    /**
     * Cập nhật thông tin bản ghi tồn kho (vị trí kệ, trạng thái hiển thị).
     *
     * @param id   ID bản ghi tồn kho
     * @param form Dữ liệu cập nhật
     * @return DTO tồn kho sau khi cập nhật
     */
    @Override
    @Transactional
    public InventoryResponseDto updateInventory(Long id, InventoryUpdateRequestForm form) {
        // Bước 1: Tìm bản ghi tồn kho theo ID
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi tồn kho với ID: " + id));

        // Bước 2: Cập nhật vị trí kệ nếu có truyền vào
        if (form.getShelfLocation() != null) {
            inventory.setShelfLocation(form.getShelfLocation().trim());
        }

        // Bước 3: Cập nhật trạng thái hiển thị nếu có truyền vào
        if (form.getStatus() != null) {
            inventory.setStatus(form.getStatus());
        }

        // Bước 4: Lưu vào cơ sở dữ liệu và trả về DTO
        Inventory savedInventory = inventoryRepository.save(inventory);
        return inventoryMapper.toDto(savedInventory);
    }

    /**
     * Thay đổi trạng thái hiển thị của bản ghi tồn kho (HIDE / UNHIDE).
     *
     * @param id     ID bản ghi tồn kho
     * @param status Trạng thái mới
     * @return DTO tồn kho sau khi đổi trạng thái
     */
    @Override
    @Transactional
    public InventoryResponseDto changeInventoryStatus(Long id, DisplayStatus status) {
        // Bước 1: Tìm bản ghi tồn kho theo ID
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi tồn kho với ID: " + id));

        // Bước 2: Gán trạng thái mới
        inventory.setStatus(status != null ? status : DisplayStatus.UNHIDE);

        // Bước 3: Lưu và trả về kết quả
        Inventory savedInventory = inventoryRepository.save(inventory);
        return inventoryMapper.toDto(savedInventory);
    }

    /**
     * Nhập thêm số lượng sách vào kho chi nhánh.
     * Tự động cộng dồn số lượng vào cả tổng số lượng (totalQuantity) và số lượng khả dụng (availableQuantity).
     *
     * @param id   ID bản ghi tồn kho
     * @param form Form chứa số lượng nhập thêm
     * @return DTO tồn kho sau khi nhập thêm
     */
    @Override
    @Transactional
    public InventoryResponseDto importStock(Long id, InventoryImportRequestForm form) {
        // Bước 1: Tìm bản ghi tồn kho theo ID
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi tồn kho với ID: " + id));

        // Bước 2: Kiểm tra số lượng nhập hợp lệ
        if (form.getQuantity() == null || form.getQuantity() <= 0) {
            throw new BadRequestException("Số lượng nhập kho phải lớn hơn 0!");
        }

        // Bước 3: Cộng dồn số lượng vào tổng số lượng (totalQuantity) và số lượng khả dụng (availableQuantity)
        int currentTotal = inventory.getTotalQuantity() != null ? inventory.getTotalQuantity() : 0;
        int currentAvailable = inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0;

        inventory.setTotalQuantity(currentTotal + form.getQuantity());
        inventory.setAvailableQuantity(currentAvailable + form.getQuantity());

        // Bước 4: Lưu vào cơ sở dữ liệu và trả về DTO
        Inventory savedInventory = inventoryRepository.save(inventory);
        return inventoryMapper.toDto(savedInventory);
    }
}
