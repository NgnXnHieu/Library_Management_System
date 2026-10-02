package com.library.specification;

import com.library.entity.Category;
import com.library.requestform.category.CategoryFilterRequestForm;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Xây dựng các tiêu chí truy vấn động (Specification) cho bảng categories bằng JPA Criteria API.
 */
public final class CategorySpecification {

    private CategorySpecification() {
        // Utility class không cho phép khởi tạo đối tượng
    }

    /**
     * Tạo Specification lọc danh sách thể loại theo các tiêu chí từ CategoryFilterRequestForm.
     *
     * @param filter Đối tượng chứa các tham số lọc: name, description, status
     * @return Specification<Category> dùng cho truy vấn Spring Data JPA
     */
    public static Specification<Category> filter(CategoryFilterRequestForm filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Nếu không truyền bộ lọc thì lấy tất cả các bản ghi
            if (filter == null) {
                return cb.conjunction();
            }

            // Bước 1: Lọc theo tên thể loại (name) - tìm kiếm gần đúng, không phân biệt hoa thường
            if (filter.getName() != null && !filter.getName().trim().isEmpty()) {
                String pattern = "%" + filter.getName().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            // Bước 2: Lọc theo mô tả thể loại (description) - tìm kiếm gần đúng, không phân biệt hoa thường
            if (filter.getDescription() != null && !filter.getDescription().trim().isEmpty()) {
                String pattern = "%" + filter.getDescription().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("description")), pattern));
            }

            // Bước 3: Lọc theo trạng thái hiển thị (status) - so khớp chính xác enum DisplayStatus
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // Kết hợp tất cả điều kiện bằng toán tử AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
