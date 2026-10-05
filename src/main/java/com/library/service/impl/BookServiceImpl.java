package com.library.service.impl;

import com.library.dto.book.BookBranchInventoryDto;
import com.library.dto.book.BookDetailCustomerResponseDto;
import com.library.dto.book.BookResponseDto;
import com.library.entity.Book;
import com.library.entity.Branch;
import com.library.entity.Category;
import com.library.entity.Inventory;
import com.library.enums.BranchStatus;
import com.library.enums.DisplayStatus;
import com.library.exception.AppException;
import com.library.exception.BadRequestException;
import com.library.exception.ErrorCode;
import com.library.mapper.BookMapper;
import com.library.repository.BookRepository;
import com.library.repository.BorrowItemRepository;
import com.library.repository.BranchRepository;
import com.library.repository.CategoryRepository;
import com.library.repository.InventoryRepository;
import com.library.requestform.book.BookCreateRequestForm;
import com.library.requestform.book.BookFilterRequestForm;
import com.library.requestform.book.BookUpdateRequestForm;
import com.library.service.BookService;
import com.library.specification.BookSpecification;
import com.library.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service triển khai các nghiệp vụ quản lý đầu sách.
 */
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BranchRepository branchRepository;
    private final InventoryRepository inventoryRepository;
    private final BorrowItemRepository borrowItemRepository;
    private final BookMapper bookMapper;

    /**
     * Thêm mới một đầu sách vào hệ thống (Chỉ dành cho ADMIN).
     * Tự động khởi tạo bản ghi Inventory với số lượng 0 cho tất cả chi nhánh hiện có.
     *
     * @param form Dữ liệu tạo mới sách
     * @return DTO thông tin sách vừa tạo
     */
    @Override
    @Transactional
    public BookResponseDto createBook(BookCreateRequestForm form) {
        // Bước 1: Kiểm tra thể loại sách có tồn tại trong hệ thống hay không
        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND, form.getCategoryId()));

        // Bước 2: Chuẩn hóa và kiểm tra mã ISBN đã tồn tại trên hệ thống chưa
        String isbn = form.getIsbn().trim().toUpperCase();
        if (bookRepository.existsByIsbn(isbn)) {
            throw new AppException(ErrorCode.BOOK_ALREADY_EXISTS, isbn);
        }

        // Bước 3: Ánh xạ toàn bộ dữ liệu từ Form và Category sang Entity bằng MapStruct
        Book book = bookMapper.toEntity(form, category);
        book.setIsbn(isbn);

        // Bước 3.1: Kiểm tra ràng buộc trạng thái: Không cho phép tạo sách UNHIDE nếu thể loại đang là HIDE
        if (book.getStatus() == DisplayStatus.UNHIDE && category.getStatus() == DisplayStatus.HIDE) {
            throw new BadRequestException("Không thể tạo đầu sách ở trạng thái hiển thị (UNHIDE) vì thể loại '" 
                    + category.getName() + "' đang ở trạng thái ẩn (HIDE). Vui lòng hiển thị thể loại trước!");
        }

        // Bước 4: Lưu đầu sách mới vào Database
        Book savedBook = bookRepository.save(book);

        // Bước 4.1: Tự động khởi tạo tồn kho (Inventory) số lượng 0 cho tất cả chi nhánh hiện có
        List<Branch> allBranches = branchRepository.findAll();
        if (!allBranches.isEmpty()) {
            List<Inventory> newInventories = new ArrayList<>();
            for (Branch branch : allBranches) {
                Inventory inventory = Inventory.builder()
                        .branch(branch)
                        .book(savedBook)
                        .totalQuantity(0)
                        .availableQuantity(0)
                        .status(DisplayStatus.UNHIDE)
                        .shelfLocation(null)
                        .build();
                newInventories.add(inventory);
            }
            inventoryRepository.saveAll(newInventories);
        }

        // Bước 5: Chuyển đổi Entity sang DTO và trả về kết quả
        return bookMapper.toDto(savedBook);
    }

    /**
     * Lấy danh sách toàn bộ các đầu sách kèm bộ lọc, sắp xếp và phân trang (Public).
     * Sử dụng JOIN FETCH Category để tối ưu hiệu năng và tránh N+1 query.
     *
     * @param filter Bộ lọc tìm kiếm và phân trang
     * @return Trang kết quả chứa danh sách BookResponseDto
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BookResponseDto> getAllBooks(BookFilterRequestForm filter) {
        if (filter == null) {
            filter = new BookFilterRequestForm();
        }

        // Đối với API công khai cho khách hàng: Chỉ hiển thị sách có trạng thái UNHIDE
        if (filter.getStatus() == null) {
            filter.setStatus(DisplayStatus.UNHIDE);
        }

        // Bước 1: Xác định hướng và trường sắp xếp (Sort)
        Sort.Direction direction = (filter.getSortDir() != null && "asc".equalsIgnoreCase(filter.getSortDir()))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        String sortBy = (filter.getSortBy() != null && !filter.getSortBy().trim().isEmpty())
                ? filter.getSortBy().trim()
                : "id";
        Sort sort = Sort.by(direction, sortBy);

        // Bước 2: Khởi tạo Pageable
        int page = Math.max(filter.getPage(), 0);
        int size = filter.getSize() > 0 ? filter.getSize() : 10;
        Pageable pageable = PageRequest.of(page, size, sort);

        // Bước 3: Thực hiện truy vấn JPQL Constructor Expression trực tiếp lên DTO
        return bookRepository.findBooksWithFilter(filter, pageable);
    }

    /**
     * Cập nhật thông tin đầu sách theo ID (Chỉ dành cho ADMIN).
     * Các trường null trong form sẽ được giữ nguyên dữ liệu hiện tại.
     *
     * @param id   ID của đầu sách cần cập nhật
     * @param form Dữ liệu cập nhật sách
     * @return DTO thông tin sách sau khi cập nhật
     */
    @Override
    @Transactional
    public BookResponseDto updateBook(Long id, BookUpdateRequestForm form) {
        // Bước 1: Tìm kiếm sách theo ID, ném ngoại lệ nếu không tìm thấy
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND, id));

        // Bước 2: Kiểm tra và cập nhật thể loại nếu có truyền categoryId
        if (form.getCategoryId() != null) {
            Category category = categoryRepository.findById(form.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND, form.getCategoryId()));
            book.setCategory(category);
        }

        // Bước 3: Kiểm tra và cập nhật mã ISBN nếu có truyền
        if (form.getIsbn() != null && !form.getIsbn().trim().isEmpty()) {
            String newIsbn = form.getIsbn().trim().toUpperCase();
            if (!newIsbn.equalsIgnoreCase(book.getIsbn()) && bookRepository.existsByIsbnAndIdNot(newIsbn, id)) {
                throw new AppException(ErrorCode.BOOK_ALREADY_EXISTS, newIsbn);
            }
            book.setIsbn(newIsbn);
        }

        // Lưu lại ảnh bìa cũ trước khi cập nhật để dọn dẹp nếu có ảnh mới
        String oldCoverImageKey = book.getCoverImageKey();

        // Bước 4: Ánh xạ các trường còn lại từ form sang entity qua MapStruct (tự động bỏ qua các trường null)
        bookMapper.updateEntityFromForm(form, book);

        // Bước 4.1: Kiểm tra ràng buộc trạng thái: Nếu sách chuyển sang UNHIDE thì thể loại phải là UNHIDE
        if (book.getStatus() == DisplayStatus.UNHIDE 
                && book.getCategory() != null 
                && book.getCategory().getStatus() == DisplayStatus.HIDE) {
            throw new BadRequestException("Không thể chuyển đầu sách sang trạng thái hiển thị (UNHIDE) vì thể loại '" 
                    + book.getCategory().getName() + "' đang ở trạng thái ẩn (HIDE). Vui lòng hiển thị thể loại trước!");
        }

        // Bước 5: Chuẩn hóa khoảng trắng cho các trường chuỗi ký tự nếu được cập nhật
        if (form.getTitle() != null && !form.getTitle().trim().isEmpty()) {
            book.setTitle(form.getTitle().trim());
        }
        if (form.getAuthor() != null) {
            book.setAuthor(form.getAuthor().trim().isEmpty() ? null : form.getAuthor().trim());
        }
        if (form.getPublisher() != null) {
            book.setPublisher(form.getPublisher().trim().isEmpty() ? null : form.getPublisher().trim());
        }

        // Bước 5.1: Xử lý dọn dẹp ảnh bìa cũ nếu có ảnh mới được thay thế
        if (form.getCoverImageKey() != null && !form.getCoverImageKey().trim().isEmpty()) {
            String newCoverImageKey = form.getCoverImageKey().trim();
            if (oldCoverImageKey != null && !oldCoverImageKey.equals(newCoverImageKey)) {
                FileUtil.deleteFile(oldCoverImageKey);
            }
            book.setCoverImageKey(newCoverImageKey);
        }

        // Bước 6: Lưu đầu sách đã cập nhật vào Database
        Book updatedBook = bookRepository.save(book);

        // Bước 6.1: Nếu sách chuyển sang HIDE -> Tự động chuyển tất cả tồn kho của sách ở các chi nhánh sang HIDE
        if (updatedBook.getStatus() == DisplayStatus.HIDE) {
            inventoryRepository.updateStatusByBookId(updatedBook.getId(), DisplayStatus.HIDE);
        }

        // Bước 7: Chuyển đổi Entity sang DTO và trả về kết quả
        return bookMapper.toDto(updatedBook);
    }

    /**
     * Lấy danh sách phân trang các đầu sách kèm theo bộ lọc mở rộng và sắp xếp (Dành riêng cho ADMIN).
     *
     * @param filter Bộ lọc tìm kiếm và thông tin phân trang
     * @return Trang kết quả chứa danh sách BookResponseDto
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BookResponseDto> getBooksWithFilter(BookFilterRequestForm filter) {
        if (filter == null) {
            filter = new BookFilterRequestForm();
        }

        // Bước 1: Xác định hướng và trường sắp xếp (Sort)
        Sort.Direction direction = (filter.getSortDir() != null && "asc".equalsIgnoreCase(filter.getSortDir()))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        String sortBy = (filter.getSortBy() != null && !filter.getSortBy().trim().isEmpty())
                ? filter.getSortBy().trim()
                : "id";
        Sort sort = Sort.by(direction, sortBy);

        // Bước 2: Khởi tạo Pageable
        int page = Math.max(filter.getPage(), 0);
        int size = filter.getSize() > 0 ? filter.getSize() : 10;
        Pageable pageable = PageRequest.of(page, size, sort);

        // Bước 3: Thực hiện truy vấn JPQL Constructor Expression trực tiếp lên DTO
        return bookRepository.findBooksWithFilter(filter, pageable);
    }

    /**
     * Lấy thông tin chi tiết một đầu sách kèm danh sách tồn kho theo chi nhánh (Public dành cho khách hàng).
     *
     * Logic nghiệp vụ:
     * - Bước 1: Kiểm tra sách có tồn tại và trạng thái hiển thị (UNHIDE) hay không.
     * - Bước 2: Nếu có truyền branchId, kiểm tra chi nhánh có tồn tại và đang mở cửa (OPEN) hay không.
     * - Bước 3: Lấy thông tin chi tiết sách kèm thể loại qua Named Native Query và @SqlResultSetMapping.
     * - Bước 4: Tạo link ảnh bìa coverImageUrl từ coverImageKey qua FileUtil.
     * - Bước 5: Lấy danh sách tồn kho khả dụng qua Named Native Query (nếu có branchId thì lấy của chi nhánh đó, ngược lại lấy tất cả chi nhánh đang mở).
     * - Bước 6: Đóng gói và trả về DTO hoàn chỉnh.
     *
     * @param bookId   ID của đầu sách (bắt buộc)
     * @param branchId ID của chi nhánh (tùy chọn)
     * @return DTO thông tin chi tiết sách kèm tồn kho
     */
    @Override
    @Transactional(readOnly = true)
    public BookDetailCustomerResponseDto getBookDetailForCustomer(Long bookId, Long branchId) {
        // Bước 1: Kiểm tra sách có tồn tại trong hệ thống hay không
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND, bookId));

        // Kiểm tra trạng thái hiển thị của sách: Chỉ hiển thị sách có trạng thái UNHIDE cho khách hàng
        if (book.getStatus() == DisplayStatus.HIDE) {
            throw new AppException(ErrorCode.BOOK_HIDDEN, book.getTitle());
        }

        // Bước 2: Nếu có truyền branchId, kiểm tra chi nhánh có tồn tại và đang mở cửa không
        if (branchId != null) {
            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND, branchId));

            if (branch.getStatus() == BranchStatus.CLOSED) {
                throw new AppException(ErrorCode.BRANCH_CLOSED, branch.getName());
            }
        }

        // Bước 3: Lấy thông tin chi tiết sách kèm tên thể loại qua @SqlResultSetMapping
        BookDetailCustomerResponseDto bookDetail = bookRepository.findBookDetailCustomerById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND, bookId));

        // Bước 4: Sinh URL ảnh bìa từ coverImageKey
        bookDetail.setCoverImageUrl(FileUtil.buildFileUrl(bookDetail.getCoverImageKey()));

        // Bước 5: Lấy danh sách tồn kho theo chi nhánh qua @SqlResultSetMapping
        List<BookBranchInventoryDto> inventories;
        if (branchId != null) {
            // Trường hợp 1: Có truyền branchId -> Chỉ lấy tồn kho của chi nhánh đó
            inventories = inventoryRepository.findCustomerInventoriesByBookAndBranch(bookId, branchId);
        } else {
            // Trường hợp 2: Không truyền branchId -> Lấy toàn bộ tồn kho tại tất cả chi nhánh đang mở
            inventories = inventoryRepository.findAllCustomerInventoriesByBook(bookId);
        }

        // Bước 6: Gán danh sách tồn kho vào DTO chi tiết sách và trả về
        bookDetail.setInventories(inventories);
        return bookDetail;
    }

    /**
     * Xóa đầu sách khỏi hệ thống (Chỉ dành cho ADMIN).
     * Kiểm tra sách có giao dịch mượn chưa (BorrowItem). Nếu chưa, xóa các bản ghi tồn kho liên quan (Inventory),
     * xóa sách trong database và dọn dẹp ảnh bìa trên hệ thống lưu trữ.
     *
     * @param id ID của đầu sách cần xóa
     */
    @Override
    @Transactional
    public void deleteBook(Long id) {
        // Bước 1: Kiểm tra đầu sách có tồn tại trong hệ thống hay không
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND, id));

        // Bước 2: Kiểm tra xem sách đã phát sinh giao dịch mượn sách chưa
        if (borrowItemRepository.existsByInventoryBookId(id)) {
            throw new AppException(ErrorCode.BOOK_CANNOT_DELETE, book.getTitle());
        }

        // Bước 3: Xóa toàn bộ bản ghi tồn kho (Inventory) liên quan đến đầu sách này
        inventoryRepository.deleteAllByBookId(id);

        // Bước 4: Xóa đầu sách khỏi cơ sở dữ liệu
        String coverImageKey = book.getCoverImageKey();
        bookRepository.delete(book);

        // Bước 5: Dọn dẹp tệp ảnh bìa nếu tồn tại
        if (coverImageKey != null && !coverImageKey.trim().isEmpty()) {
            FileUtil.deleteFile(coverImageKey);
        }
    }
}
