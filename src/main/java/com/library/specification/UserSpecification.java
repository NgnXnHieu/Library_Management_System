package com.library.specification;

import com.library.entity.User;
import com.library.requestform.user.UserFilterRequestForm;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Xây dựng các tiêu chí truy vấn động (Specification) cho bảng users bằng JPA
 * Criteria API.
 * Thực hiện JOIN FETCH với bảng accounts để nạp sẵn thông tin tài khoản và
 * chống N+1 query.
 */
public final class UserSpecification {

    private UserSpecification() {
        // Lớp tiện ích, ngăn chặn việc khởi tạo instance
    }

    /**
     * Tạo Specification lọc danh sách người dùng theo các tiêu chí từ
     * UserFilterRequestForm.
     *
     * @param filter Đối tượng chứa các tham số lọc người dùng
     * @return Specification<User> dùng cho truy vấn Spring Data JPA
     */
    public static Specification<User> filter(UserFilterRequestForm filter) {
        return (root, query, cb) -> {
            // Bước 1: Thực hiện JOIN FETCH bảng accounts để lấy sẵn thông tin tài khoản
            // (tránh N+1 query)
            // Chỉ fetch khi câu truy vấn là DATA Query (lấy dữ liệu thực).
            // Nếu câu truy vấn là COUNT Query (đếm bản ghi - getResultType() là Long),
            // ta không thực hiện fetch để tránh lỗi SemanticException của Hibernate.
            if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("account", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            // Bước 2: Lọc theo từ khóa search (tìm kiếm trong: username, fullName, phone,
            // email)
            if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
                String searchTrimmed = filter.getSearch().trim();
                String lowerKeyword = "%" + searchTrimmed.toLowerCase() + "%";
                String rawKeyword = "%" + searchTrimmed + "%";

                Predicate pUsername = cb.like(cb.lower(root.get("account").get("username")), lowerKeyword);
                Predicate pFullName = cb.like(cb.lower(root.get("fullName")), lowerKeyword);
                Predicate pPhone = cb.like(root.get("phone"), rawKeyword);
                Predicate pEmail = cb.like(cb.lower(root.get("email")), lowerKeyword);

                // Thỏa mãn bất kỳ 1 trong 4 điều kiện
                predicates.add(cb.or(pUsername, pFullName, pPhone, pEmail));
            }

            // Bước 3: Trả về điều kiện truy vấn tổng hợp
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
