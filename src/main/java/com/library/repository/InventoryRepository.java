package com.library.repository;

import com.library.dto.book.BookBranchInventoryDto;
import com.library.entity.Inventory;
import com.library.enums.DisplayStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác với bảng inventories trong Database.
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory>, InventoryRepositoryCustom {

    /**
     * Lấy thông tin tồn kho sách tại một chi nhánh cụ thể (dành cho khách hàng).
     * Chỉ lấy chi nhánh đang OPEN và trạng thái tồn kho UNHIDE.
     * Sử dụng Named Native Query "Inventory.findCustomerInventoriesByBookAndBranch" và @SqlResultSetMapping "BookBranchInventoryMapping".
     *
     * @param bookId   ID của đầu sách
     * @param branchId ID của chi nhánh
     * @return Danh sách DTO tồn kho chi nhánh
     */
    @Query(name = "Inventory.findCustomerInventoriesByBookAndBranch", nativeQuery = true)
    List<BookBranchInventoryDto> findCustomerInventoriesByBookAndBranch(
            @Param("bookId") Long bookId,
            @Param("branchId") Long branchId
    );

    /**
     * Lấy toàn bộ thông tin tồn kho sách tại tất cả các chi nhánh đang mở (dành cho khách hàng).
     * Chỉ lấy chi nhánh đang OPEN và trạng thái tồn kho UNHIDE, sắp xếp theo tên chi nhánh.
     * Sử dụng Named Native Query "Inventory.findAllCustomerInventoriesByBook" và @SqlResultSetMapping "BookBranchInventoryMapping".
     *
     * @param bookId ID của đầu sách
     * @return Danh sách DTO tồn kho chi nhánh
     */
    @Query(name = "Inventory.findAllCustomerInventoriesByBook", nativeQuery = true)
    List<BookBranchInventoryDto> findAllCustomerInventoriesByBook(@Param("bookId") Long bookId);

    /**
     * Lấy danh sách ID các cuốn sách đã có trong kho của một chi nhánh.
     *
     * @param branchId ID chi nhánh
     * @return Danh sách ID sách đã tồn tại trong kho
     */
    @Query("SELECT i.book.id FROM Inventory i WHERE i.branch.id = :branchId")
    List<Long> findBookIdsByBranchId(@Param("branchId") Long branchId);

    /**
     * Kiểm tra cuốn sách đã tồn tại trong kho của chi nhánh hay chưa.
     *
     * @param branchId ID chi nhánh
     * @param bookId   ID sách
     * @return true nếu đã có trong kho, ngược lại false
     */
    boolean existsByBranchIdAndBookId(Long branchId, Long bookId);

    /**
     * Tìm bản ghi tồn kho theo cặp chi nhánh và sách.
     *
     * @param branchId ID chi nhánh
     * @param bookId   ID sách
     * @return Optional chứa Inventory nếu tìm thấy
     */
    Optional<Inventory> findByBranchIdAndBookId(Long branchId, Long bookId);

    /**
     * Lấy toàn bộ danh sách tồn kho của một chi nhánh.
     *
     * @param branchId ID chi nhánh
     * @return Danh sách các bản ghi Inventory của chi nhánh
     */
    List<Inventory> findAllByBranchId(Long branchId);

    /**
     * Lấy danh sách tồn kho theo danh sách ID và chi nhánh, kết hợp JOIN FETCH sách để tránh N+1 query.
     *
     * @param branchId ID chi nhánh
     * @param ids      Danh sách ID tồn kho cần tìm
     * @return Danh sách Inventory kèm thông tin Book
     */
    @Query("SELECT i FROM Inventory i " +
           "JOIN FETCH i.book b " +
           "WHERE i.branch.id = :branchId AND i.id IN :ids")
    List<Inventory> findAllByBranchIdAndIdInWithBook(@Param("branchId") Long branchId, @Param("ids") Collection<Long> ids);

    /**
     * Xóa toàn bộ bản ghi tồn kho của một đầu sách (dùng khi xóa sách).
     *
     * @param bookId ID cuốn sách
     */
    void deleteAllByBookId(Long bookId);

    /**
     * Cập nhật trạng thái hiển thị của tất cả tồn kho thuộc chi nhánh chỉ định.
     * Dùng khi cập nhật chi nhánh sang trạng thái CLOSED.
     *
     * @param branchId ID chi nhánh
     * @param status   Trạng thái hiển thị mới
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.status = :status WHERE i.branch.id = :branchId")
    void updateStatusByBranchId(@Param("branchId") Long branchId, @Param("status") DisplayStatus status);

    /**
     * Cập nhật trạng thái hiển thị của tất cả tồn kho thuộc cuốn sách chỉ định.
     * Dùng khi cập nhật sách sang trạng thái HIDE.
     *
     * @param bookId ID cuốn sách
     * @param status Trạng thái hiển thị mới
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.status = :status WHERE i.book.id = :bookId")
    void updateStatusByBookId(@Param("bookId") Long bookId, @Param("status") DisplayStatus status);

    /**
     * Cập nhật trạng thái hiển thị của tất cả tồn kho thuộc các sách của thể loại chỉ định.
     * Dùng khi cập nhật thể loại sang trạng thái HIDE.
     *
     * @param categoryId ID thể loại
     * @param status     Trạng thái hiển thị mới
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.status = :status WHERE i.book.id IN (SELECT b.id FROM Book b WHERE b.category.id = :categoryId)")
    void updateStatusByBookCategoryId(@Param("categoryId") Long categoryId, @Param("status") DisplayStatus status);
}
