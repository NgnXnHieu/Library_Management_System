package com.library.specification;

import com.library.entity.Account;
import com.library.entity.BorrowSlip;
import com.library.entity.User;
import com.library.requestform.borrow.BorrowSlipFilterRequestForm;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Xây dựng các tiêu chí truy vấn động (Specification) cho bảng borrow_slips bằng JPA Criteria API.
 * Kết hợp JOIN FETCH (khách hàng, tài khoản, nhân viên, chi nhánh) để tránh lỗi N+1 Query.
 */
public final class BorrowSlipSpecification {

    private BorrowSlipSpecification() {
        // Utility class - ngăn chặn khởi tạo instance
    }

    /**
     * Tạo Specification lọc danh sách phiếu mượn theo các tiêu chí từ BorrowSlipFilterRequestForm.
     *
     * @param filter Đối tượng chứa các tham số tìm kiếm và lọc
     * @return Specification<BorrowSlip> dùng cho truy vấn Spring Data JPA
     */
    public static Specification<BorrowSlip> filter(BorrowSlipFilterRequestForm filter) {
        return (root, query, cb) -> {
            // Bước 1: Thực hiện JOIN FETCH nạp sẵn dữ liệu liên quan để tránh N+1 Query.
            // Chỉ fetch khi câu truy vấn là DATA Query (lấy dữ liệu thực).
            // Bỏ qua khi Hibernate chạy COUNT Query (getResultType() là Long) để tránh lỗi ngoại lệ SemanticException.
            if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
                Fetch<BorrowSlip, User> customerFetch = root.fetch("customer", JoinType.LEFT);
                customerFetch.fetch("account", JoinType.LEFT);
                root.fetch("staff", JoinType.LEFT);
                root.fetch("branch", JoinType.LEFT);
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            // Bước 2: Lọc theo thời gian mượn sách (từ ngày - đến ngày)
            if (filter.getFromBorrowedAt() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("borrowedAt"), filter.getFromBorrowedAt()));
            }
            if (filter.getToBorrowedAt() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("borrowedAt"), filter.getToBorrowedAt()));
            }

            // Bước 3: Lọc theo thời gian trả sách (từ ngày - đến ngày)
            if (filter.getFromReturnedAt() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("returnedAt"), filter.getFromReturnedAt()));
            }
            if (filter.getToReturnedAt() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("returnedAt"), filter.getToReturnedAt()));
            }

            // Bước 4: Lọc theo mã phiếu mượn (tìm kiếm gần đúng / chứa chuỗi)
            if (StringUtils.hasText(filter.getBorrowCode())) {
                predicates.add(cb.like(
                        cb.lower(root.get("borrowCode")),
                        "%" + filter.getBorrowCode().trim().toLowerCase() + "%"
                ));
            }

            // Bước 5: Tìm kiếm thông tin khách hàng chung (kiểm tra phone, email, username, fullName)
            if (StringUtils.hasText(filter.getCustomerSearch())) {
                Join<BorrowSlip, User> customerJoin = root.join("customer", JoinType.LEFT);
                Join<User, Account> accountJoin = customerJoin.join("account", JoinType.LEFT);

                String keyword = "%" + filter.getCustomerSearch().trim().toLowerCase() + "%";
                Predicate phonePredicate = cb.like(cb.lower(customerJoin.get("phone")), keyword);
                Predicate emailPredicate = cb.like(cb.lower(customerJoin.get("email")), keyword);
                Predicate usernamePredicate = cb.like(cb.lower(accountJoin.get("username")), keyword);
                Predicate fullNamePredicate = cb.like(cb.lower(customerJoin.get("fullName")), keyword);

                predicates.add(cb.or(phonePredicate, emailPredicate, usernamePredicate, fullNamePredicate));
            }

            // Bước 6: Lọc theo ID chi nhánh
            if (filter.getBranchId() != null) {
                predicates.add(cb.equal(root.get("branch").get("id"), filter.getBranchId()));
            }

            // Bước 7: Lọc theo trạng thái phiếu mượn (BORROWED, RETURNED, OVERDUE)
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // Bước 8: Lọc theo trạng thái thanh toán (UNPAID, PAID)
            if (filter.getPaymentStatus() != null) {
                predicates.add(cb.equal(root.get("paymentStatus"), filter.getPaymentStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
