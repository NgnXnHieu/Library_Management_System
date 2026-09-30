package com.library.repository;

import com.library.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
