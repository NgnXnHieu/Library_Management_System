package com.library.specification;

import com.library.entity.User;
import com.library.requestform.user.UserAdminFilterRequestForm;
import com.library.requestform.user.UserFilterRequestForm;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Xây dựng các tiêu chí truy vấn động (Specification) cho bảng users bằng JPA Criteria API.
 * Thực hiện JOIN FETCH với bảng accounts, branches, roles để nạp sẵn thông tin và chống N+1 query.
 */
public final class UserSpecification {

    private UserSpecification() {
        // Lớp tiện ích, ngăn chặn việc khởi tạo instance
    }

    /**
     * Tạo Specification lọc danh sách người dùng theo các tiêu chí từ UserFilterRequestForm.
     *
     * @param filter Đối tượng chứa các tham số lọc người dùng
     * @return Specification<User> dùng cho truy vấn Spring Data JPA
     */
    public static Specification<User> filter(UserFilterRequestForm filter) {
        return (root, query, cb) -> {
            // Bước 1: Thực hiện JOIN FETCH bảng account và role để lấy sẵn thông tin (tránh N+1 query)
            // Chỉ fetch khi câu truy vấn là DATA Query (lấy dữ liệu thực).
            // Nếu câu truy vấn là COUNT Query (đếm bản ghi - getResultType() là Long),
            // ta không thực hiện fetch để tránh lỗi SemanticException của Hibernate.
            if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("account", JoinType.LEFT);
                root.fetch("role", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            // Bước 2: Lọc theo từ khóa search (tìm kiếm trong: username, fullName, phone, email)
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

            // Bước 3: Lọc theo vai trò (role) nếu có truyền vào
            if (filter.getRole() != null && !filter.getRole().trim().isEmpty()) {
                predicates.add(cb.equal(cb.upper(root.get("role").get("code")), filter.getRole().trim().toUpperCase()));
            }

            // Bước 4: Lọc theo trạng thái tài khoản (status) nếu có truyền vào
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // Bước 5: Trả về điều kiện truy vấn tổng hợp
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Tạo Specification lọc và phân trang danh sách người dùng dành riêng cho ADMIN.
     * Tự động thực hiện JOIN FETCH bảng account, branch và role khi câu truy vấn là DATA Query để nạp đủ dữ liệu
     * cho UserResponseDto và tránh N+1 query.
     *
     * @param filter Đối tượng chứa các tham số lọc phân trang người dùng
     * @return Specification<User> dùng cho truy vấn Spring Data JPA
     */
    public static Specification<User> filterAdmin(UserAdminFilterRequestForm filter) {
        return (root, query, cb) -> {
            // Bước 1: Thực hiện JOIN FETCH với account, branch và role để tối ưu hóa truy vấn
            // Chỉ fetch khi là DATA Query, không fetch khi là COUNT Query để tránh lỗi Hibernate
            if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("account", JoinType.LEFT);
                root.fetch("branch", JoinType.LEFT);
                root.fetch("role", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            // Bước 2: Lọc theo từ khóa searchName (tìm kiếm đồng thời trong: username, email, phone)
            if (filter.getSearchName() != null && !filter.getSearchName().trim().isEmpty()) {
                String searchTrimmed = filter.getSearchName().trim();
                String lowerKeyword = "%" + searchTrimmed.toLowerCase() + "%";
                String rawKeyword = "%" + searchTrimmed + "%";

                Predicate pUsername = cb.like(cb.lower(root.get("account").get("username")), lowerKeyword);
                Predicate pEmail = cb.like(cb.lower(root.get("email")), lowerKeyword);
                Predicate pPhone = cb.like(root.get("phone"), rawKeyword);

                predicates.add(cb.or(pUsername, pEmail, pPhone));
            }

            // Bước 3: Lọc theo vai trò (role: theo code hoặc name của role)
            if (filter.getRole() != null && !filter.getRole().trim().isEmpty()) {
                String roleKeyword = "%" + filter.getRole().trim().toLowerCase() + "%";
                Predicate pRoleCode = cb.like(cb.lower(root.get("role").get("code")), roleKeyword);
                Predicate pRoleName = cb.like(cb.lower(root.get("role").get("name")), roleKeyword);
                predicates.add(cb.or(pRoleCode, pRoleName));
            }

            // Bước 4: Lọc theo tên chi nhánh (branchName)
            if (filter.getBranchName() != null && !filter.getBranchName().trim().isEmpty()) {
                String branchKeyword = "%" + filter.getBranchName().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("branch").get("name")), branchKeyword));
            }

            // Bước 5: Lọc theo danh sách trạng thái tài khoản (statuses)
            if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(filter.getStatuses()));
            }

            // Bước 6: Trả về điều kiện truy vấn tổng hợp
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
