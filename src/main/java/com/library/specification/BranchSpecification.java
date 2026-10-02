package com.library.specification;

import com.library.entity.Branch;
import com.library.requestform.branch.BranchFilterRequestForm;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Xây dựng các tiêu chí truy vấn động (Specification) cho bảng branches bằng JPA Criteria API.
 */
public final class BranchSpecification {

    private BranchSpecification() {
        // Utility class không cho phép khởi tạo đối tượng
    }

    /**
     * Tạo Specification lọc danh sách chi nhánh theo các tiêu chí từ BranchFilterRequestForm.
     *
     * @param filter Đối tượng chứa các tham số lọc: code, name, address, phone, status
     * @return Specification<Branch> dùng cho truy vấn Spring Data JPA
     */
    public static Specification<Branch> filter(BranchFilterRequestForm filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Nếu không truyền bộ lọc thì lấy tất cả các bản ghi
            if (filter == null) {
                return cb.conjunction();
            }

            // Bước 1: Lọc theo mã chi nhánh (code) - tìm kiếm gần đúng, không phân biệt hoa thường
            if (filter.getCode() != null && !filter.getCode().trim().isEmpty()) {
                String pattern = "%" + filter.getCode().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("code")), pattern));
            }

            // Bước 2: Lọc theo tên chi nhánh (name) - tìm kiếm gần đúng, không phân biệt hoa thường
            if (filter.getName() != null && !filter.getName().trim().isEmpty()) {
                String pattern = "%" + filter.getName().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            // Bước 3: Lọc theo địa chỉ chi nhánh (address) - tìm kiếm gần đúng, không phân biệt hoa thường
            if (filter.getAddress() != null && !filter.getAddress().trim().isEmpty()) {
                String pattern = "%" + filter.getAddress().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("address")), pattern));
            }

            // Bước 4: Lọc theo số điện thoại (phone) - tìm kiếm gần đúng
            if (filter.getPhone() != null && !filter.getPhone().trim().isEmpty()) {
                String pattern = "%" + filter.getPhone().trim() + "%";
                predicates.add(cb.like(root.get("phone"), pattern));
            }

            // Bước 5: Lọc theo trạng thái hoạt động (status) - so khớp chính xác giá trị enum (OPEN, CLOSED)
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // Kết hợp tất cả điều kiện bằng toán tử AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
