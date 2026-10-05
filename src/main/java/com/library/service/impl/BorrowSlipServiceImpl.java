package com.library.service.impl;

import com.library.dto.borrow.BorrowItemResponseDto;
import com.library.dto.borrow.BorrowSlipResponseDto;
import com.library.entity.Account;
import com.library.entity.BorrowItem;
import com.library.entity.BorrowSlip;
import com.library.entity.Branch;
import com.library.entity.Inventory;
import com.library.entity.Payment;
import com.library.entity.User;
import com.library.enums.AccountStatus;
import com.library.enums.BorrowStatus;
import com.library.enums.PaymentMethod;
import com.library.enums.PaymentPurpose;
import com.library.enums.PaymentStatus;
import com.library.exception.AppException;
import com.library.exception.BadRequestException;
import com.library.exception.ErrorCode;
import com.library.mapper.BorrowSlipMapper;
import com.library.repository.AccountRepository;
import com.library.repository.BorrowItemRepository;
import com.library.repository.BorrowSlipRepository;
import com.library.repository.BranchRepository;
import com.library.repository.InventoryRepository;
import com.library.repository.PaymentRepository;
import com.library.repository.UserRepository;
import com.library.requestform.borrow.BorrowItemRequestForm;
import com.library.requestform.borrow.BorrowSlipCreateRequestForm;
import com.library.requestform.borrow.BorrowSlipFilterRequestForm;
import com.library.security.UserDetailCustom;
import com.library.service.BorrowSlipService;
import com.library.specification.BorrowSlipSpecification;
import com.library.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lớp triển khai các phương thức nghiệp vụ quản lý phiếu mượn sách
 * (BorrowSlipService).
 */
@Service
@RequiredArgsConstructor
public class BorrowSlipServiceImpl implements BorrowSlipService {

    private final BorrowSlipRepository borrowSlipRepository;
    private final BorrowItemRepository borrowItemRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BranchRepository branchRepository;
    private final PaymentRepository paymentRepository;
    private final BorrowSlipMapper borrowSlipMapper;

    /**
     * Tạo mới phiếu mượn sách với branchId và staffId chỉ định.
     * Sử dụng @Transactional để đảm bảo toàn vẹn dữ liệu: kiểm tra kho, trừ tồn kho
     * và lưu phiếu mượn.
     *
     * @param form     Dữ liệu tạo phiếu mượn gồm customerId và danh sách tồn kho
     *                 sách kèm số lượng
     * @param branchId ID chi nhánh thực hiện mượn sách
     * @param staffId  ID nhân viên tạo phiếu mượn
     * @return DTO thông tin phiếu mượn vừa được tạo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BorrowSlipResponseDto createBorrowSlip(BorrowSlipCreateRequestForm form, Long branchId, Long staffId) {
        // Bước 1: Kiểm tra khách hàng tồn tại và đang hoạt động
        User customer = userRepository.findById(form.getCustomerId())
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND_BY_ID, form.getCustomerId()));

        if (customer.getStatus() != null && customer.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Tài khoản khách hàng đang bị khóa hoặc ngừng hoạt động!");
        }

        // Bước 2: Kiểm tra nhân viên tạo phiếu tồn tại
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND_BY_ID, staffId));

        // Bước 3: Kiểm tra chi nhánh thực hiện mượn sách tồn tại
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND, branchId));

        // Bước 4: Kiểm tra danh sách sách mượn không rỗng và tổng hợp số lượng mượn
        // theo từng inventoryId trên RAM
        if (form.getItems() == null || form.getItems().isEmpty()) {
            throw new BadRequestException("Danh sách sách mượn không được để trống!");
        }

        Map<Long, Integer> requestedQuantityMap = new LinkedHashMap<>();
        for (BorrowItemRequestForm item : form.getItems()) {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BadRequestException("Số lượng mượn của từng sách phải lớn hơn 0!");
            }
            requestedQuantityMap.merge(item.getInventoryId(), item.getQuantity(), Integer::sum);
        }

        // Bước 5: Truy vấn 1 lần duy nhất lấy toàn bộ tồn kho thuộc chi nhánh kèm thông
        // tin sách (tránh N+1 query)
        List<Inventory> inventories = inventoryRepository.findAllByBranchIdAndIdInWithBook(branchId,
                requestedQuantityMap.keySet());
        Map<Long, Inventory> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(Inventory::getId, Function.identity()));

        // Bước 6: Kiểm tra tính hợp lệ trên RAM
        // 6.1. Kiểm tra tất cả inventoryId yêu cầu có tồn tại và thuộc đúng chi nhánh
        // hay không
        for (Long invId : requestedQuantityMap.keySet()) {
            if (!inventoryMap.containsKey(invId)) {
                throw new BadRequestException(
                        "Sách trong kho với ID " + invId + " không tồn tại hoặc không thuộc chi nhánh hiện tại!");
            }
        }

        // 6.2. Kiểm tra nếu có bất kỳ sách nào trong kho có số lượng khả dụng bằng 0
        // thì báo lỗi ngay
        for (Long invId : requestedQuantityMap.keySet()) {
            Inventory inventory = inventoryMap.get(invId);
            int availableQty = inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0;
            if (availableQty <= 0) {
                String bookTitle = (inventory.getBook() != null && inventory.getBook().getTitle() != null)
                        ? inventory.getBook().getTitle()
                        : "ID " + invId;
                throw new BadRequestException(String.format(
                        "Sách '%s' trong kho hiện tại đã hết (số lượng bằng 0), không thể thực hiện mượn!",
                        bookTitle));
            }
        }

        // 6.3. Kiểm tra số lượng sách khả dụng trong kho có đủ đáp ứng số lượng mượn
        // yêu cầu hay không
        for (Map.Entry<Long, Integer> entry : requestedQuantityMap.entrySet()) {
            Long invId = entry.getKey();
            Integer requestedQty = entry.getValue();
            Inventory inventory = inventoryMap.get(invId);
            int availableQty = inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0;

            if (availableQty < requestedQty) {
                String bookTitle = (inventory.getBook() != null && inventory.getBook().getTitle() != null)
                        ? inventory.getBook().getTitle()
                        : "ID: " + invId;
                throw new BadRequestException(String.format(
                        "Số lượng sách '%s' trong kho không đủ để mượn! (Yêu cầu: %d, Khả dụng: %d)",
                        bookTitle, requestedQty, availableQty));
            }
        }

        // Bước 7: Cập nhật trừ số lượng khả dụng trong kho và tính tổng số lượng, tổng
        // tiền thuê trên RAM
        int totalQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : requestedQuantityMap.entrySet()) {
            Inventory inventory = inventoryMap.get(entry.getKey());
            int qty = entry.getValue();

            // Trừ số lượng khả dụng trong kho
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - qty);

            // Tính tổng số lượng
            totalQuantity += qty;

            // Tính tổng tiền thuê: số lượng * giá thuê (rentalPrice)
            BigDecimal rentalPrice = (inventory.getBook() != null && inventory.getBook().getRentalPrice() != null)
                    ? inventory.getBook().getRentalPrice()
                    : BigDecimal.ZERO;
            BigDecimal itemTotal = rentalPrice.multiply(BigDecimal.valueOf(qty));
            totalAmount = totalAmount.add(itemTotal);
        }

        // Cập nhật các bản ghi tồn kho vào cơ sở dữ liệu
        inventoryRepository.saveAll(inventoryMap.values());

        // Bước 8: Tạo mới bản ghi phiếu mượn (BorrowSlip)
        LocalDateTime borrowedAt = LocalDateTime.now();
        LocalDateTime dueAt = borrowedAt.plusDays(5);
        String borrowCode = generateUniqueBorrowCode();

        // Kiểm tra phương thức thanh toán: nếu khách trả tiền mặt (CASH) ngay tại quầy
        // thì cập nhật PAID, ngược lại UNPAID
        boolean isCashPaid = form.getPaymentMethod() == PaymentMethod.CASH;
        PaymentStatus initialPaymentStatus = isCashPaid ? PaymentStatus.PAID : PaymentStatus.UNPAID;

        BorrowSlip borrowSlip = BorrowSlip.builder()
                .borrowCode(borrowCode)
                .customer(customer)
                .staff(staff)
                .branch(branch)
                .borrowedAt(borrowedAt)
                .dueAt(dueAt)
                .status(BorrowStatus.BORROWED)
                .paymentStatus(initialPaymentStatus)
                .totalQuantity(totalQuantity)
                .totalAmount(totalAmount)
                .build();

        BorrowSlip savedBorrowSlip = borrowSlipRepository.save(borrowSlip);

        // Bước 9: Tạo các chi tiết mượn sách tương ứng (BorrowItem) kèm đơn giá chốt tại thời điểm mượn
        List<BorrowItem> borrowItems = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : requestedQuantityMap.entrySet()) {
            Inventory inventory = inventoryMap.get(entry.getKey());
            BigDecimal itemRentalPrice = (inventory.getBook() != null && inventory.getBook().getRentalPrice() != null)
                    ? inventory.getBook().getRentalPrice()
                    : BigDecimal.ZERO;

            BorrowItem borrowItem = BorrowItem.builder()
                    .borrowSlip(savedBorrowSlip)
                    .inventory(inventory)
                    .quantity(entry.getValue())
                    .rentalPrice(itemRentalPrice)
                    .build();
            borrowItems.add(borrowItem);
        }

        borrowItemRepository.saveAll(borrowItems);
        savedBorrowSlip.setBorrowItems(borrowItems);

        // Bước 10: Nếu thanh toán bằng tiền mặt ngay tại quầy, tự động tạo và lưu bản
        // ghi giao dịch Payment
        if (isCashPaid) {
            String paymentCode = generateUniquePaymentCode();
            Payment payment = Payment.builder()
                    .paymentCode(paymentCode)
                    .borrowSlip(savedBorrowSlip)
                    .customer(customer)
                    .amount(totalAmount)
                    .paymentMethod(PaymentMethod.CASH)
                    .paymentPurpose(PaymentPurpose.RENTAL_FEE)
                    .provider("CASH")
                    .status(PaymentStatus.PAID)
                    .paidAt(borrowedAt)
                    .build();
            paymentRepository.save(payment);
            savedBorrowSlip.getPayments().add(payment);
        }

        // Bước 11: Ánh xạ kết quả sang DTO qua MapStruct và trả về
        return borrowSlipMapper.toDto(savedBorrowSlip);
    }

    /**
     * Tạo mới phiếu mượn sách tại chi nhánh làm việc của nhân viên đang đăng nhập.
     * Tự động trích xuất branchId và staffId từ SecurityContextHolder.
     *
     * @param form Dữ liệu tạo phiếu mượn gồm customerId và danh sách tồn kho sách
     *             kèm số lượng
     * @return DTO thông tin phiếu mượn vừa được tạo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BorrowSlipResponseDto createBorrowSlipForCurrentStaffBranch(BorrowSlipCreateRequestForm form) {
        // Bước 1: Trích xuất branchId của nhân viên đang đăng nhập
        Long branchId = SecurityUtil.getCurrentBranchId()
                .orElseThrow(() -> new BadRequestException("Tài khoản nhân viên chưa được gán chi nhánh hoạt động!"));

        // Bước 2: Trích xuất staffId của nhân viên đang đăng nhập
        Long staffId = SecurityUtil.getRequiredUserId();

        // Bước 3: Tái sử dụng logic tạo phiếu mượn chung
        return createBorrowSlip(form, branchId, staffId);
    }

    /**
     * Lấy danh sách phiếu mượn phân trang kèm bộ lọc và sắp xếp (Dành cho Admin,
     * cho phép lọc theo branchId).
     *
     * @param filter Bộ lọc tìm kiếm, sắp xếp và phân trang
     * @return Trang kết quả chứa danh sách phiếu mượn
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BorrowSlipResponseDto> getAllBorrowSlips(BorrowSlipFilterRequestForm filter) {
        if (filter == null) {
            filter = new BorrowSlipFilterRequestForm();
        }

        // Bước 1: Xác định hướng và trường sắp xếp (Sort) - mặc định theo ngày mượn (borrowedAt) từ cũ -> mới (ASC)
        Sort.Direction direction = (filter.getSortDir() != null
                && "desc".equalsIgnoreCase(filter.getSortDir()))
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        String rawSortBy = (filter.getSortBy() != null && !filter.getSortBy().trim().isEmpty())
                ? filter.getSortBy().trim()
                : "borrowedAt";

        // Ánh xạ các trường sắp xếp theo yêu cầu: ngày mượn (borrowedAt), ngày trả (returnedAt), giá tiền (totalAmount)
        String sortBy = switch (rawSortBy) {
            case "returnedAt" -> "returnedAt";
            case "totalAmount" -> "totalAmount";
            default -> "borrowedAt";
        };

        Sort sort = Sort.by(direction, sortBy);

        // Bước 2: Khởi tạo Pageable
        int page = filter.getPage() >= 0 ? filter.getPage() : 0;
        int size = filter.getSize() > 0 ? filter.getSize() : 10;
        Pageable pageable = PageRequest.of(page, size, sort);

        // Bước 3: Thực hiện truy vấn phân trang JPQL Constructor Expression chiếu trực tiếp lên DTO
        Page<BorrowSlipResponseDto> borrowSlipPage = borrowSlipRepository.findAllBorrowSlipsWithFilter(filter, pageable);

        if (borrowSlipPage.isEmpty()) {
            return Page.empty(pageable);
        }

        // Bước 4: Trích xuất danh sách ID của các phiếu mượn trong trang hiện tại
        List<Long> borrowSlipIds = borrowSlipPage.getContent().stream()
                .map(BorrowSlipResponseDto::getId)
                .toList();

        // Bước 5: Truy vấn bảng borrow_items theo danh sách ID (kết hợp JOIN FETCH inventory và book)
        List<BorrowItem> borrowItems = borrowItemRepository.findAllByBorrowSlipIdInWithBook(borrowSlipIds);

        // Bước 6: Gom nhóm và ánh xạ các borrowItem sang BorrowItemResponseDto theo borrowSlipId vào Map
        Map<Long, List<BorrowItemResponseDto>> itemsBySlipId = borrowItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getBorrowSlip().getId(),
                        Collectors.mapping(borrowSlipMapper::toBorrowItemDto, Collectors.toList())
                ));

        // Bước 7: Gán danh sách items tương ứng vào từng BorrowSlipResponseDto cho frontend
        borrowSlipPage.forEach(slip -> {
            List<BorrowItemResponseDto> slipItems = itemsBySlipId.getOrDefault(slip.getId(), new ArrayList<>());
            slip.setItems(slipItems);
        });

        return borrowSlipPage;
    }

    /**
     * Lấy danh sách phiếu mượn phân trang theo chi nhánh của nhân viên/quản lý đang
     * đăng nhập.
     * branchId được tự động trích xuất từ tài khoản hiện tại và gán vào bộ lọc.
     *
     * @param filter Bộ lọc tìm kiếm, sắp xếp và phân trang
     * @return Trang kết quả chứa danh sách phiếu mượn của chi nhánh
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BorrowSlipResponseDto> getBorrowSlipsByCurrentBranch(BorrowSlipFilterRequestForm filter) {
        // Bước 1: Trích xuất branchId của nhân viên/quản lý đang đăng nhập từ
        // SecurityContextHolder
        Long branchId = SecurityUtil.getCurrentBranchId()
                .orElseThrow(() -> new BadRequestException("Tài khoản chưa được gán chi nhánh hoạt động!"));

        // Bước 2: Thiết lập chi nhánh bắt buộc cho bộ lọc tìm kiếm (ghi đè mọi branchId
        // client truyền lên)
        if (filter == null) {
            filter = new BorrowSlipFilterRequestForm();
        }
        filter.setBranchId(branchId);

        // Bước 3: Tái sử dụng logic truy vấn phân trang chung
        return getAllBorrowSlips(filter);
    }

    /**
     * Hủy phiếu mượn sách, hoàn trả số lượng tồn kho sách về chi nhánh và cập nhật
     * trạng thái thanh toán.
     * Áp dụng @Transactional để đảm bảo toàn vẹn dữ liệu cho phiếu, payment và tồn
     * kho.
     *
     * @param id ID của phiếu mượn cần hủy
     * @return DTO thông tin phiếu mượn sau khi hủy
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BorrowSlipResponseDto cancelBorrowSlip(Long id) {
        // Bước 1: Kiểm tra phiếu mượn có tồn tại hay không
        BorrowSlip borrowSlip = borrowSlipRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BORROW_SLIP_NOT_FOUND, id));

        // Bước 2: Kiểm tra trạng thái phiếu mượn (chỉ cho phép hủy khi đang ở trạng
        // thái BORROWED)
        if (borrowSlip.getStatus() != BorrowStatus.BORROWED) {
            throw new BadRequestException("Chỉ có thể hủy phiếu mượn đang ở trạng thái Đang mượn (BORROWED)!");
        }

        // Bước 3: Kiểm tra branchId của tài khoản hiện tại có trùng khớp với branchId
        // trên phiếu mượn không
        Long currentBranchId = SecurityUtil.getCurrentBranchId()
                .orElseThrow(() -> new BadRequestException("Tài khoản chưa được gán chi nhánh hoạt động!"));

        if (borrowSlip.getBranch() == null || !currentBranchId.equals(borrowSlip.getBranch().getId())) {
            throw new BadRequestException("Bạn không có quyền hủy phiếu mượn của chi nhánh khác!");
        }

        // Bước 4: Kiểm tra giao dịch thanh toán tiền thuê (type = RENTAL_FEE)
        List<Payment> rentalPayments = paymentRepository.findByBorrowSlipIdAndPaymentPurpose(id,
                PaymentPurpose.RENTAL_FEE);

        // 4.1. Nếu có giao dịch chuyển khoản (BANK_TRANSFER) mà trạng thái đã thanh
        // toán (PAID) -> Tạm thời chưa hỗ trợ
        boolean hasPaidBankTransfer = rentalPayments.stream().anyMatch(
                p -> p.getPaymentMethod() == PaymentMethod.BANK_TRANSFER && p.getStatus() == PaymentStatus.PAID);
        if (hasPaidBankTransfer) {
            throw new BadRequestException(
                    "Hệ thống hiện tại chưa hỗ trợ hủy phiếu mượn đã thanh toán qua hình thức chuyển khoản!");
        }

        // 4.2. Cập nhật trạng thái cho các Payment tiền thuê tương ứng
        for (Payment payment : rentalPayments) {
            if (payment.getPaymentMethod() == PaymentMethod.CASH && payment.getStatus() == PaymentStatus.PAID) {
                // Tiền mặt đã thu -> hoàn trả tiền mặt cho khách
                payment.setStatus(PaymentStatus.REFUNDED);
            } else if (payment.getStatus() == PaymentStatus.UNPAID) {
                // Chưa thanh toán (dù là chuyển khoản hay tiền mặt) -> hủy giao dịch
                payment.setStatus(PaymentStatus.CANCELLED);
            }
        }
        if (!rentalPayments.isEmpty()) {
            paymentRepository.saveAll(rentalPayments);
        }

        // Cập nhật paymentStatus của phiếu mượn tương ứng
        if (borrowSlip.getPaymentStatus() == PaymentStatus.PAID) {
            borrowSlip.setPaymentStatus(PaymentStatus.REFUNDED);
        } else {
            borrowSlip.setPaymentStatus(PaymentStatus.CANCELLED);
        }

        // Bước 5: Cập nhật trạng thái phiếu mượn sang CANCELLED
        borrowSlip.setStatus(BorrowStatus.CANCELLED);

        // Bước 6: Tổng hợp danh sách inventoryId cùng quantity cần cộng hoàn trả lại
        // kho
        Map<Long, Integer> inventoryQuantityMap = new LinkedHashMap<>();
        if (borrowSlip.getBorrowItems() != null) {
            for (BorrowItem item : borrowSlip.getBorrowItems()) {
                if (item.getInventory() != null && item.getQuantity() != null && item.getQuantity() > 0) {
                    inventoryQuantityMap.merge(item.getInventory().getId(), item.getQuantity(), Integer::sum);
                }
            }
        }

        // Bước 7: Gọi hàm cộng ngược số lượng về inventories tương ứng
        restoreInventoryQuantities(inventoryQuantityMap);

        // Bước 8: Lưu thông tin phiếu mượn và trả về DTO
        BorrowSlip updatedBorrowSlip = borrowSlipRepository.save(borrowSlip);
        return borrowSlipMapper.toDto(updatedBorrowSlip);
    }

    /**
     * Cập nhật trạng thái phiếu mượn sách dành riêng cho Admin (BORROWED, RETURNED, OVERDUE, CANCELLED).
     * Tự động hoàn trả tồn kho sách khi chuyển sang RETURNED hoặc CANCELLED.
     * Quản lý giao dịch với @Transactional để bảo đảm tính toàn vẹn dữ liệu.
     *
     * @param id     ID của phiếu mượn cần cập nhật
     * @param status Trạng thái mới của phiếu mượn
     * @return DTO thông tin phiếu mượn sau khi cập nhật
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BorrowSlipResponseDto updateBorrowSlipStatus(Long id, BorrowStatus status) {
        // Bước 1: Kiểm tra dữ liệu đầu vào
        if (status == null) {
            throw new BadRequestException("Trạng thái mới không được để trống!");
        }

        // Bước 2: Kiểm tra phiếu mượn có tồn tại hay không
        BorrowSlip borrowSlip = borrowSlipRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BORROW_SLIP_NOT_FOUND, id));

        BorrowStatus oldStatus = borrowSlip.getStatus();
        if (oldStatus == status) {
            // Không thay đổi trạng thái, trả về thông tin hiện tại
            return borrowSlipMapper.toDto(borrowSlip);
        }

        // Bước 3: Xử lý theo từng trạng thái mới được chọn
        switch (status) {
            case RETURNED -> {
                // Chỉ cho phép chuyển sang RETURNED từ BORROWED hoặc OVERDUE
                if (oldStatus == BorrowStatus.CANCELLED) {
                    throw new BadRequestException("Phiếu mượn đã bị hủy, không thể chuyển sang trạng thái Đã trả!");
                }
                // Ghi nhận thời điểm hoàn trả nếu chưa có
                if (borrowSlip.getReturnedAt() == null) {
                    borrowSlip.setReturnedAt(LocalDateTime.now());
                }
                // Hoàn trả số lượng sách về kho của chi nhánh
                restoreSlipInventories(borrowSlip);
                borrowSlip.setStatus(BorrowStatus.RETURNED);
            }
            case CANCELLED -> {
                // Chỉ cho phép hủy khi đang BORROWED hoặc OVERDUE
                if (oldStatus == BorrowStatus.RETURNED) {
                    throw new BadRequestException("Phiếu mượn đã được hoàn trả sách, không thể hủy!");
                }
                // Hoàn trả số lượng sách về kho của chi nhánh
                restoreSlipInventories(borrowSlip);

                // Cập nhật trạng thái thanh toán và các payment liên quan
                List<Payment> rentalPayments = paymentRepository.findByBorrowSlipIdAndPaymentPurpose(id, PaymentPurpose.RENTAL_FEE);
                for (Payment payment : rentalPayments) {
                    if (payment.getPaymentMethod() == PaymentMethod.CASH && payment.getStatus() == PaymentStatus.PAID) {
                        payment.setStatus(PaymentStatus.REFUNDED);
                    } else if (payment.getStatus() == PaymentStatus.UNPAID) {
                        payment.setStatus(PaymentStatus.CANCELLED);
                    }
                }
                if (!rentalPayments.isEmpty()) {
                    paymentRepository.saveAll(rentalPayments);
                }

                if (borrowSlip.getPaymentStatus() == PaymentStatus.PAID) {
                    borrowSlip.setPaymentStatus(PaymentStatus.REFUNDED);
                } else {
                    borrowSlip.setPaymentStatus(PaymentStatus.CANCELLED);
                }

                borrowSlip.setStatus(BorrowStatus.CANCELLED);
            }
            case OVERDUE -> {
                // Chỉ cho phép chuyển sang OVERDUE nếu đang BORROWED
                if (oldStatus == BorrowStatus.RETURNED || oldStatus == BorrowStatus.CANCELLED) {
                    throw new BadRequestException("Phiếu mượn đã hoàn tất hoặc bị hủy, không thể đánh dấu Quá hạn!");
                }
                borrowSlip.setStatus(BorrowStatus.OVERDUE);
            }
            case BORROWED -> {
                // Cho phép khôi phục từ OVERDUE về BORROWED
                if (oldStatus == BorrowStatus.RETURNED || oldStatus == BorrowStatus.CANCELLED) {
                    throw new BadRequestException("Phiếu mượn đã hoàn tất hoặc bị hủy, không thể chuyển ngược lại Đang mượn!");
                }
                borrowSlip.setStatus(BorrowStatus.BORROWED);
            }
        }

        // Bước 4: Lưu thông tin phiếu mượn và trả về DTO
        BorrowSlip updatedBorrowSlip = borrowSlipRepository.save(borrowSlip);
        return borrowSlipMapper.toDto(updatedBorrowSlip);
    }

    /**
     * Cập nhật trạng thái phiếu mượn sách tại chi nhánh của nhân viên/quản lý đang đăng nhập.
     * Kiểm tra chi nhánh của tài khoản hiện tại phải khớp với chi nhánh của phiếu mượn.
     * Áp dụng @Transactional để đảm bảo toàn vẹn dữ liệu.
     *
     * @param id     ID của phiếu mượn cần cập nhật
     * @param status Trạng thái mới của phiếu mượn (BORROWED, RETURNED, OVERDUE, CANCELLED)
     * @return DTO thông tin phiếu mượn sau khi cập nhật
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BorrowSlipResponseDto updateBorrowSlipStatusForBranch(Long id, BorrowStatus status) {
        // Bước 1: Kiểm tra dữ liệu đầu vào
        if (status == null) {
            throw new BadRequestException("Trạng thái mới không được để trống!");
        }

        // Bước 2: Kiểm tra phiếu mượn có tồn tại hay không
        BorrowSlip borrowSlip = borrowSlipRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BORROW_SLIP_NOT_FOUND, id));

        // Bước 3: Trích xuất branchId của tài khoản nhân viên/quản lý đang đăng nhập từ SecurityContextHolder
        Long currentBranchId = SecurityUtil.getCurrentBranchId()
                .orElseThrow(() -> new BadRequestException("Tài khoản chưa được gán chi nhánh hoạt động!"));

        // Bước 4: Kiểm tra chi nhánh của tài khoản có trùng khớp với chi nhánh của phiếu mượn không
        if (borrowSlip.getBranch() == null || !borrowSlip.getBranch().getId().equals(currentBranchId)) {
            throw new BadRequestException("Bạn không có quyền cập nhật phiếu mượn của chi nhánh khác!");
        }

        // Bước 5: Tái sử dụng logic cập nhật trạng thái phiếu mượn
        return updateBorrowSlipStatus(id, status);
    }

    /**
     * Hàm phụ trợ gom danh sách inventoryId và số lượng để hoàn trả kho từ phiếu mượn.
     *
     * @param borrowSlip Phiếu mượn cần hoàn trả sách về kho
     */
    private void restoreSlipInventories(BorrowSlip borrowSlip) {
        Map<Long, Integer> inventoryQuantityMap = new LinkedHashMap<>();
        if (borrowSlip.getBorrowItems() != null) {
            for (BorrowItem item : borrowSlip.getBorrowItems()) {
                if (item.getInventory() != null && item.getQuantity() != null && item.getQuantity() > 0) {
                    inventoryQuantityMap.merge(item.getInventory().getId(), item.getQuantity(), Integer::sum);
                }
            }
        }
        restoreInventoryQuantities(inventoryQuantityMap);
    }

    /**
     * Hàm phụ trợ cộng ngược số lượng sách về tồn kho (inventories) tương ứng.
     * Sử dụng 1 lệnh truy vấn findAllById duy nhất để lấy toàn bộ invent ories cần
     * cập nhật (tránh N+1 query).
     *
     * @param inventoryQuantityMap Map chứa cặp (inventoryId -> số lượng cần hoàn
     *                             trả)
     */
    private void restoreInventoryQuantities(Map<Long, Integer> inventoryQuantityMap) {
        if (inventoryQuantityMap == null || inventoryQuantityMap.isEmpty()) {
            return;
        }

        // Dùng 1 lệnh lấy toàn bộ inventories theo danh sách ID
        List<Inventory> inventories = inventoryRepository.findAllById(inventoryQuantityMap.keySet());

        // Duyệt và cộng lại số lượng vào inventories tương ứng
        for (Inventory inventory : inventories) {
            Integer restoreQty = inventoryQuantityMap.get(inventory.getId());
            if (restoreQty != null && restoreQty > 0) {
                int currentAvailable = inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0;
                inventory.setAvailableQuantity(currentAvailable + restoreQty);
            }
        }

        // Lưu cập nhật hàng loạt vào cơ sở dữ liệu
        inventoryRepository.saveAll(inventories);
    }

    /**
     * Hàm phụ trợ sinh mã phiếu mượn duy nhất (định dạng BS-<timestamp>-<random>).
     *
     * @return Chuỗi mã phiếu mượn duy nhất (tối đa 30 ký tự)
     */
    private String generateUniqueBorrowCode() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String suffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "BS-" + timestamp + "-" + suffix;
    }

    /**
     * Hàm phụ trợ sinh mã giao dịch thanh toán duy nhất (định dạng
     * PAY-<timestamp>-<random>).
     *
     * @return Chuỗi mã thanh toán duy nhất (tối đa 30 ký tự)
     */
    private String generateUniquePaymentCode() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String suffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "PAY-" + timestamp + "-" + suffix;
    }

    /**
     * Lấy danh sách phiếu mượn phân trang kèm chi tiết sách (borrowItems) cho tài khoản đang đăng nhập.
     * Tự động trích xuất accountId, username từ SecurityContextHolder để kiểm tra trạng thái tồn tại và khóa tài khoản,
     * sau đó lọc chính xác theo userId của người dùng.
     *
     * @param filter Bộ lọc trạng thái mượn, trạng thái thanh toán, sắp xếp ngày mượn/trả và phân trang
     * @return Trang kết quả chứa danh sách phiếu mượn của người dùng hiện tại
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BorrowSlipResponseDto> getMyBorrowSlips(BorrowSlipFilterRequestForm filter) {
        // Bước 1: Trích xuất thông tin người dùng đang đăng nhập từ SecurityContextHolder
        UserDetailCustom currentUser = SecurityUtil.getCurrentUserOrThrow();
        Long accountId = currentUser.getAccountId();
        String username = currentUser.getUsername();

        // Bước 2: Kiểm tra Account trong Database có tồn tại và có bị khóa không
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if ("LOCKED".equalsIgnoreCase(account.getStatus()) || "INACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new AppException(ErrorCode.ACCOUNT_INACTIVE_OR_LOCKED);
        }

        // Bước 3: Kiểm tra User trong Database có tồn tại và có bị khóa không
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == AccountStatus.LOCKED || user.getStatus() == AccountStatus.INACTIVE) {
            throw new AppException(ErrorCode.ACCOUNT_INACTIVE_OR_LOCKED);
        }

        // Bước 4: Thiết lập customerId bắt buộc vào bộ lọc là userId của người dùng hiện tại
        if (filter == null) {
            filter = new BorrowSlipFilterRequestForm();
        }
        filter.setCustomerId(user.getId());

        // Bước 5: Gọi hàm truy vấn phân trang chung kết hợp Specification đã tối ưu JOIN FETCH và @BatchSize
        return getAllBorrowSlips(filter);
    }
}
