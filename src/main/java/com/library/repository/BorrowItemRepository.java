package com.library.repository;

import com.library.entity.BorrowItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Truy vấn danh sách chi tiết mượn sách theo danh sách ID phiếu mượn.
     * Sử dụng LEFT JOIN FETCH với inventory và book để nạp sẵn dữ liệu sách, tránh phát sinh N+1 Query.
     *
     * @param borrowSlipIds Danh sách ID các phiếu mượn
     * @return Danh sách chi tiết mượn sách kèm thông tin tồn kho và sách
     */
    @Query("SELECT bi FROM BorrowItem bi " +
           "LEFT JOIN FETCH bi.inventory inv " +
           "LEFT JOIN FETCH inv.book b " +
           "WHERE bi.borrowSlip.id IN :borrowSlipIds")
    List<BorrowItem> findAllByBorrowSlipIdInWithBook(@Param("borrowSlipIds") List<Long> borrowSlipIds);
}
