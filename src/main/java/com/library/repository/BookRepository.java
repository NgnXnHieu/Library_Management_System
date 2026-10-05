package com.library.repository;

import com.library.dto.book.BookDetailCustomerResponseDto;
import com.library.dto.book.BookResponseDto;
import com.library.entity.Book;
import com.library.enums.DisplayStatus;
import com.library.requestform.book.BookFilterRequestForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác với bảng books trong Database.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    /**
     * Lấy danh sách đầu sách phân trang kết hợp bộ lọc đa tiêu chí bằng JPQL Constructor Expression.
     * Chiếu trực tiếp kết quả lên BookResponseDto, loại bỏ việc nạp thực thể và cột description vào RAM.
     *
     * @param filter   Bộ lọc tìm kiếm (trạng thái, thể loại, từ khóa, khoảng giá,...)
     * @param pageable Thông tin phân trang và sắp xếp
     * @return Trang kết quả chứa danh sách BookResponseDto
     */
    @Query(value = "SELECT new com.library.dto.book.BookResponseDto("
            + "b.id, c.id, c.name, b.isbn, b.title, b.author, b.publisher, b.publicationYear, "
            + "b.coverImageKey, b.price, b.rentalPrice, b.fineAmount, b.status, b.createdAt, b.updatedAt) "
            + "FROM Book b LEFT JOIN b.category c "
            + "WHERE (:#{#filter.status} IS NULL OR b.status = :#{#filter.status}) "
            + "AND (:#{#filter.categoryId} IS NULL OR c.id = :#{#filter.categoryId}) "
            + "AND (:#{#filter.isbn} IS NULL OR TRIM(:#{#filter.isbn}) = '' OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.isbn}), '%'))) "
            + "AND (:#{#filter.title} IS NULL OR TRIM(:#{#filter.title}) = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.title}), '%'))) "
            + "AND (:#{#filter.author} IS NULL OR TRIM(:#{#filter.author}) = '' OR LOWER(b.author) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.author}), '%'))) "
            + "AND (:#{#filter.publisher} IS NULL OR TRIM(:#{#filter.publisher}) = '' OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.publisher}), '%'))) "
            + "AND (:#{#filter.publicationYear} IS NULL OR b.publicationYear = :#{#filter.publicationYear}) "
            + "AND (:#{#filter.publicationYearFrom} IS NULL OR b.publicationYear >= :#{#filter.publicationYearFrom}) "
            + "AND (:#{#filter.publicationYearTo} IS NULL OR b.publicationYear <= :#{#filter.publicationYearTo}) "
            + "AND (:#{#filter.minPrice} IS NULL OR b.price >= :#{#filter.minPrice}) "
            + "AND (:#{#filter.maxPrice} IS NULL OR b.price <= :#{#filter.maxPrice}) "
            + "AND (:#{#filter.minRentalPrice} IS NULL OR b.rentalPrice >= :#{#filter.minRentalPrice}) "
            + "AND (:#{#filter.maxRentalPrice} IS NULL OR b.rentalPrice <= :#{#filter.maxRentalPrice}) "
            + "AND (:#{#filter.minFineAmount} IS NULL OR b.fineAmount >= :#{#filter.minFineAmount}) "
            + "AND (:#{#filter.maxFineAmount} IS NULL OR b.fineAmount <= :#{#filter.maxFineAmount})",
            countQuery = "SELECT count(b) FROM Book b LEFT JOIN b.category c "
            + "WHERE (:#{#filter.status} IS NULL OR b.status = :#{#filter.status}) "
            + "AND (:#{#filter.categoryId} IS NULL OR c.id = :#{#filter.categoryId}) "
            + "AND (:#{#filter.isbn} IS NULL OR TRIM(:#{#filter.isbn}) = '' OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.isbn}), '%'))) "
            + "AND (:#{#filter.title} IS NULL OR TRIM(:#{#filter.title}) = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.title}), '%'))) "
            + "AND (:#{#filter.author} IS NULL OR TRIM(:#{#filter.author}) = '' OR LOWER(b.author) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.author}), '%'))) "
            + "AND (:#{#filter.publisher} IS NULL OR TRIM(:#{#filter.publisher}) = '' OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.publisher}), '%'))) "
            + "AND (:#{#filter.publicationYear} IS NULL OR b.publicationYear = :#{#filter.publicationYear}) "
            + "AND (:#{#filter.publicationYearFrom} IS NULL OR b.publicationYear >= :#{#filter.publicationYearFrom}) "
            + "AND (:#{#filter.publicationYearTo} IS NULL OR b.publicationYear <= :#{#filter.publicationYearTo}) "
            + "AND (:#{#filter.minPrice} IS NULL OR b.price >= :#{#filter.minPrice}) "
            + "AND (:#{#filter.maxPrice} IS NULL OR b.price <= :#{#filter.maxPrice}) "
            + "AND (:#{#filter.minRentalPrice} IS NULL OR b.rentalPrice >= :#{#filter.minRentalPrice}) "
            + "AND (:#{#filter.maxRentalPrice} IS NULL OR b.rentalPrice <= :#{#filter.maxRentalPrice}) "
            + "AND (:#{#filter.minFineAmount} IS NULL OR b.fineAmount >= :#{#filter.minFineAmount}) "
            + "AND (:#{#filter.maxFineAmount} IS NULL OR b.fineAmount <= :#{#filter.maxFineAmount})")
    Page<BookResponseDto> findBooksWithFilter(@Param("filter") BookFilterRequestForm filter, Pageable pageable);

    /**
     * Lấy thông tin chi tiết đầu sách kèm thể loại dành cho khách hàng.
     * Sử dụng Named Native Query "Book.findBookDetailCustomerById" và @SqlResultSetMapping "BookDetailCustomerMapping".
     *
     * @param bookId ID của đầu sách
     * @return Optional chứa BookDetailCustomerResponseDto
     */
    @Query(name = "Book.findBookDetailCustomerById", nativeQuery = true)
    Optional<BookDetailCustomerResponseDto> findBookDetailCustomerById(@Param("bookId") Long bookId);

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

    /**
     * Cập nhật trạng thái hiển thị của tất cả các cuốn sách thuộc thể loại chỉ định.
     * Dùng khi cập nhật thể loại sang trạng thái HIDE.
     *
     * @param categoryId ID thể loại
     * @param status     Trạng thái hiển thị mới
     */
    @Modifying
    @Query("UPDATE Book b SET b.status = :status WHERE b.category.id = :categoryId")
    void updateStatusByCategoryId(@Param("categoryId") Long categoryId, @Param("status") DisplayStatus status);
}
