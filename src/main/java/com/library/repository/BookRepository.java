package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác với bảng books trong Database.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    /**
     * Tìm sách theo mã ISBN.
     *
     * @param isbn Mã ISBN của sách
     * @return Optional chứa sách nếu tìm thấy
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Kiểm tra có sách nào thuộc thể loại chỉ định không.
     *
     * @param categoryId ID của thể loại
     * @return true nếu có sách thuộc thể loại này, ngược lại false
     */
    boolean existsByCategoryId(Long categoryId);

    /**
     * Kiểm tra mã ISBN đã tồn tại trên hệ thống chưa.
     *
     * @param isbn Mã ISBN của sách
     * @return true nếu mã ISBN đã tồn tại, ngược lại false
     */
    boolean existsByIsbn(String isbn);

    /**
     * Kiểm tra mã ISBN đã tồn tại trên cuốn sách khác (khác ID chỉ định) hay chưa.
     * Phục vụ kiểm tra trùng lặp khi cập nhật thông tin sách.
     *
     * @param isbn Mã ISBN của sách
     * @param id   ID của cuốn sách hiện tại cần loại trừ
     * @return true nếu mã ISBN đã thuộc về một cuốn sách khác, ngược lại false
     */
    boolean existsByIsbnAndIdNot(String isbn, Long id);

    /**
     * Lấy toàn bộ danh sách sách kèm theo thông tin thể loại (Category) bằng JOIN FETCH để loại bỏ N+1 query.
     *
     * @return Danh sách sách đã được nạp sẵn thông tin Category
     */
    @Query("SELECT b FROM Book b JOIN FETCH b.category")
    List<Book> findAllWithCategory();
}
