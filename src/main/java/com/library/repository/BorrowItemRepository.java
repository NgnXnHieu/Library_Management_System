package com.library.repository;

import com.library.entity.BorrowItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác với bảng borrow_items trong Database.
 */
@Repository
public interface BorrowItemRepository extends JpaRepository<BorrowItem, Long> {

    /**
     * Lấy danh sách chi tiết mượn sách theo ID của phiếu mượn.
     *
     * @param borrowSlipId ID phiếu mượn
     * @return Danh sách chi tiết mượn sách
     */
    List<BorrowItem> findAllByBorrowSlipId(Long borrowSlipId);

    /**
     * Kiểm tra xem cuốn sách đã từng phát sinh trong chi tiết mượn nào chưa.
     *
     * @param bookId ID cuốn sách
     * @return true nếu đã có trong chi tiết mượn, ngược lại false
     */
    boolean existsByInventoryBookId(Long bookId);
}
