package com.library.specification;

import com.library.entity.Book;
import com.library.entity.Branch;
import com.library.entity.Inventory;
import com.library.requestform.inventory.InventoryFilterRequestForm;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Xây dựng các tiêu chí truy vấn động (Specification) cho bảng inventories bằng JPA Criteria API.
 * Kết hợp JOIN FETCH đa tầng (Inventory -> Branch, Inventory -> Book -> Category) để tránh N+1 query.
 */
public final class InventorySpecification {

    private InventorySpecification() {
        // Utility class
    }

    /**
     * Tạo Specification lọc danh sách tồn kho theo các tiêu chí từ InventoryFilterRequestForm.
     *
     * @param filter Đối tượng chứa các tham số lọc
     * @return Specification<Inventory> dùng cho truy vấn Spring Data JPA
     */
    public static Specification<Inventory> filter(InventoryFilterRequestForm filter) {
        return (root, query, cb) -> {
            // Bước 1: Thực hiện JOIN FETCH đa tầng để nạp sẵn dữ liệu Branch, Book và Category
            // Lưu ý: Chỉ fetch khi câu truy vấn là DATA Query (lấy dữ liệu thực).
            // Nếu câu truy vấn là COUNT Query (đếm bản ghi cho phân trang - getResultType() là Long),
            // ta không thực hiện fetch để tránh lỗi ngoại lệ SemanticException của Hibernate.
            if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("branch", JoinType.LEFT);
                Fetch<Inventory, Book> bookFetch = root.fetch("book", JoinType.LEFT);
                bookFetch.fetch("category", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            // Bước 2: Lọc theo ID chi nhánh
            if (filter.getBranchId() != null) {
                predicates.add(cb.equal(root.get("branch").get("id"), filter.getBranchId()));
            }

            // Bước 3: Lọc theo ID thể loại sách
            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("book").get("category").get("id"), filter.getCategoryId()));
            }

            // Bước 4: Lọc theo tiêu đề sách (tìm kiếm chứa / gần đúng không phân biệt hoa thường)
            if (filter.getBookTitle() != null && !filter.getBookTitle().trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("book").get("title")),
                        "%" + filter.getBookTitle().trim().toLowerCase() + "%"
                ));
            }

            // Bước 5: Lọc theo khoảng giá sách (giá gốc/giá bìa)
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("book").get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("book").get("price"), filter.getMaxPrice()));
            }

            // Bước 6: Lọc theo khoảng giá thuê/mượn sách (rentalPrice)
            if (filter.getMinRentalPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("book").get("rentalPrice"), filter.getMinRentalPrice()));
            }
            if (filter.getMaxRentalPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("book").get("rentalPrice"), filter.getMaxRentalPrice()));
            }

            // Bước 7: Lọc theo khoảng tiền phạt (fineAmount)
            if (filter.getMinFineAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("book").get("fineAmount"), filter.getMinFineAmount()));
            }
            if (filter.getMaxFineAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("book").get("fineAmount"), filter.getMaxFineAmount()));
            }

            // Bước 8: Lọc theo trạng thái hiển thị của kho (status)
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // Bước 9: Lọc theo vị trí kệ sách (shelfLocation)
            if (filter.getShelfLocation() != null && !filter.getShelfLocation().trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("shelfLocation")),
                        "%" + filter.getShelfLocation().trim().toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
