package com.library.repository;

import com.library.dto.user.UserResponseDto;
import com.library.entity.User;
import com.library.enums.AccountStatus;
import com.library.requestform.user.CustomerFilterRequestForm;
import com.library.requestform.user.UserAdminFilterRequestForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * Lấy danh sách người dùng phân trang kèm bộ lọc đa tiêu chí cho ADMIN bằng JPQL Constructor Expression.
     * Chiếu trực tiếp kết quả lên UserResponseDto, loại bỏ việc nạp đồng thời 4 Managed Entity (User, Account, Role, Branch) vào RAM.
     *
     * @param filter   Bộ lọc người dùng (từ khóa searchName, vai trò, chi nhánh, trạng thái)
     * @param pageable Thông tin phân trang và sắp xếp
     * @return Trang kết quả chứa danh sách UserResponseDto
     */
    @Query(value = "SELECT new com.library.dto.user.UserResponseDto("
            + "u.id, a.id, a.username, u.email, u.phone, u.fullName, u.dateOfBirth, "
            + "u.imageUrl, u.address, r.code, b.id, b.name, u.status, u.createdAt, u.updatedAt) "
            + "FROM User u "
            + "LEFT JOIN u.account a "
            + "LEFT JOIN u.role r "
            + "LEFT JOIN u.branch b "
            + "WHERE (:#{#filter.searchName} IS NULL OR TRIM(:#{#filter.searchName}) = '' OR ("
            + "   LOWER(a.username) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.searchName}), '%')) "
            + "   OR LOWER(u.email) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.searchName}), '%')) "
            + "   OR u.phone LIKE CONCAT('%', TRIM(:#{#filter.searchName}), '%'))) "
            + "AND (:#{#filter.role} IS NULL OR TRIM(:#{#filter.role}) = '' OR ("
            + "   LOWER(r.code) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.role}), '%')) "
            + "   OR LOWER(r.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.role}), '%')))) "
            + "AND (:#{#filter.branchName} IS NULL OR TRIM(:#{#filter.branchName}) = '' OR ("
            + "   LOWER(b.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.branchName}), '%')))) "
            + "AND (:#{#filter.statuses} IS NULL OR u.status IN :#{#filter.statuses})",
            countQuery = "SELECT count(u) FROM User u "
            + "LEFT JOIN u.account a "
            + "LEFT JOIN u.role r "
            + "LEFT JOIN u.branch b "
            + "WHERE (:#{#filter.searchName} IS NULL OR TRIM(:#{#filter.searchName}) = '' OR ("
            + "   LOWER(a.username) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.searchName}), '%')) "
            + "   OR LOWER(u.email) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.searchName}), '%')) "
            + "   OR u.phone LIKE CONCAT('%', TRIM(:#{#filter.searchName}), '%'))) "
            + "AND (:#{#filter.role} IS NULL OR TRIM(:#{#filter.role}) = '' OR ("
            + "   LOWER(r.code) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.role}), '%')) "
            + "   OR LOWER(r.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.role}), '%')))) "
            + "AND (:#{#filter.branchName} IS NULL OR TRIM(:#{#filter.branchName}) = '' OR ("
            + "   LOWER(b.name) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.branchName}), '%')))) "
            + "AND (:#{#filter.statuses} IS NULL OR u.status IN :#{#filter.statuses})")
    Page<UserResponseDto> findUsersWithFilter(@Param("filter") UserAdminFilterRequestForm filter, Pageable pageable);

    /**
     * Lấy danh sách khách hàng (CUSTOMER) phân trang kèm bộ lọc bằng JPQL Constructor Expression.
     * Cố định điều kiện vai trò là CUSTOMER, loại bỏ nạp thực thể vào bộ nhớ đệm.
     *
     * @param filter   Bộ lọc tìm kiếm khách hàng (username, fullName, phone, email, status)
     * @param pageable Thông tin phân trang và sắp xếp
     * @return Trang kết quả chứa danh sách UserResponseDto
     */
    @Query(value = "SELECT new com.library.dto.user.UserResponseDto("
            + "u.id, a.id, a.username, u.email, u.phone, u.fullName, u.dateOfBirth, "
            + "u.imageUrl, u.address, r.code, b.id, b.name, u.status, u.createdAt, u.updatedAt) "
            + "FROM User u "
            + "LEFT JOIN u.account a "
            + "LEFT JOIN u.role r "
            + "LEFT JOIN u.branch b "
            + "WHERE (UPPER(r.code) = 'CUSTOMER' OR UPPER(r.code) = 'ROLE_CUSTOMER') "
            + "AND (:#{#filter.username} IS NULL OR TRIM(:#{#filter.username}) = '' OR LOWER(a.username) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.username}), '%'))) "
            + "AND (:#{#filter.fullName} IS NULL OR TRIM(:#{#filter.fullName}) = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.fullName}), '%'))) "
            + "AND (:#{#filter.phone} IS NULL OR TRIM(:#{#filter.phone}) = '' OR u.phone LIKE CONCAT('%', TRIM(:#{#filter.phone}), '%')) "
            + "AND (:#{#filter.email} IS NULL OR TRIM(:#{#filter.email}) = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.email}), '%'))) "
            + "AND (:#{#filter.status} IS NULL OR u.status = :#{#filter.status})",
            countQuery = "SELECT count(u) FROM User u "
            + "LEFT JOIN u.account a "
            + "LEFT JOIN u.role r "
            + "WHERE (UPPER(r.code) = 'CUSTOMER' OR UPPER(r.code) = 'ROLE_CUSTOMER') "
            + "AND (:#{#filter.username} IS NULL OR TRIM(:#{#filter.username}) = '' OR LOWER(a.username) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.username}), '%'))) "
            + "AND (:#{#filter.fullName} IS NULL OR TRIM(:#{#filter.fullName}) = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.fullName}), '%'))) "
            + "AND (:#{#filter.phone} IS NULL OR TRIM(:#{#filter.phone}) = '' OR u.phone LIKE CONCAT('%', TRIM(:#{#filter.phone}), '%')) "
            + "AND (:#{#filter.email} IS NULL OR TRIM(:#{#filter.email}) = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', TRIM(:#{#filter.email}), '%'))) "
            + "AND (:#{#filter.status} IS NULL OR u.status = :#{#filter.status})")
    Page<UserResponseDto> findCustomersWithFilter(@Param("filter") CustomerFilterRequestForm filter, Pageable pageable);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    boolean existsByRoleId(Long roleId);

    boolean existsByBranchId(Long branchId);

    Optional<User> findByAccountId(Long accountId);

    @Query("SELECT u FROM User u " +
           "JOIN FETCH u.account a " +
           "JOIN FETCH u.role r " +
           "LEFT JOIN FETCH u.branch b " +
           "WHERE a.id = :accountId")
    Optional<User> findByAccountIdWithDetails(@Param("accountId") Long accountId);

    List<User> findByStatus(AccountStatus status);

    /**
     * Tìm kiếm nhanh danh sách độc giả (role CUSTOMER, status ACTIVE) theo từ khóa phục vụ lập phiếu mượn.
     * Sử dụng Named Native Query "User.findActiveCustomersBySearch" và @SqlResultSetMapping "CustomerSearchMapping".
     *
     * @param search Từ khóa tìm kiếm (họ tên, username, số điện thoại, email)
     * @return Danh sách UserResponseDto
     */
    @Query(name = "User.findActiveCustomersBySearch", nativeQuery = true)
    List<UserResponseDto> findActiveCustomersBySearch(@Param("search") String search);
}

