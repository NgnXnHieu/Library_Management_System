package com.library.repository;

import com.library.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {

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
}
