package com.library.specification;

import com.library.entity.Book;
import com.library.requestform.book.BookFilterRequestForm;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Xây dựng các tiêu chí truy vấn động (Specification) cho bảng books bằng JPA Criteria API.
 * Kết hợp JOIN FETCH thực thể Category để tránh N+1 query.
 */
public final class BookSpecification {

    private BookSpecification() {
        // Utility class
    }

    /**
     * Tạo Specification lọc danh sách sách theo các tiêu chí từ BookFilterRequestForm.
     *
     * @param filter Đối tượng chứa các tham số lọc
     * @return Specification<Book> dùng cho truy vấn Spring Data JPA
     */
    public static Specification<Book> filter(BookFilterRequestForm filter) {
        return (root, query, cb) -> {
            // Bước 1: Thực hiện JOIN FETCH lấy thông tin Category
            // Lưu ý: Chỉ fetch khi câu truy vấn là DATA Query (lấy dữ liệu thực).
            // Nếu câu truy vấn là COUNT Query (đếm bản ghi cho phân trang - getResultType() là Long),
            // ta không thực hiện fetch để tránh lỗi ngoại lệ SemanticException của Hibernate.
            if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("category", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            // Bước 2: Lọc theo trạng thái hiển thị (DisplayStatus: HIDE, UNHIDE)
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // Bước 3: Lọc theo mã ISBN (tìm kiếm chứa / gần đúng không phân biệt hoa thường)
            if (filter.getIsbn() != null && !filter.getIsbn().trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("isbn")),
                        "%" + filter.getIsbn().trim().toLowerCase() + "%"
                ));
            }

            // Bước 4: Lọc theo tiêu đề sách (tìm kiếm chứa / gần đúng không phân biệt hoa thường)
            if (filter.getTitle() != null && !filter.getTitle().trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("title")),
                        "%" + filter.getTitle().trim().toLowerCase() + "%"
                ));
            }

            // Bước 5: Lọc theo khoảng năm xuất bản
            if (filter.getPublicationYearFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("publicationYear"), filter.getPublicationYearFrom()));
            }
            if (filter.getPublicationYearTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("publicationYear"), filter.getPublicationYearTo()));
            }

            // Bước 6: Lọc theo khoảng giá sách (giá gốc)
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            // Bước 7: Lọc theo khoảng giá mượn (rentalPrice)
            if (filter.getMinRentalPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rentalPrice"), filter.getMinRentalPrice()));
            }
            if (filter.getMaxRentalPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rentalPrice"), filter.getMaxRentalPrice()));
            }

            // Bước 8: Lọc theo khoảng tiền phạt (fineAmount)
            if (filter.getMinFineAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fineAmount"), filter.getMinFineAmount()));
            }
            if (filter.getMaxFineAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fineAmount"), filter.getMaxFineAmount()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
