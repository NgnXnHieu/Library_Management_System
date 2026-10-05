package com.library.entity;

import com.library.dto.user.UserResponseDto;
import com.library.enums.AccountStatus;
import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NamedNativeQuery(
        name = "User.findActiveCustomersBySearch",
        query = "SELECT u.id AS userId, a.id AS accountId, a.username AS username, " +
                "u.email AS email, u.phone AS phone, u.full_name AS fullName, " +
                "r.name AS role, u.status AS status " +
                "FROM users u " +
                "JOIN accounts a ON u.account_id = a.id " +
                "JOIN roles r ON u.role_id = r.id " +
                "WHERE (r.code = 'CUSTOMER' OR r.code = 'ROLE_CUSTOMER') " +
                "AND u.status = 'ACTIVE' " +
                "AND (:search IS NULL OR :search = '' " +
                "     OR u.full_name LIKE CONCAT('%', :search, '%') " +
                "     OR u.phone LIKE CONCAT('%', :search, '%') " +
                "     OR u.email LIKE CONCAT('%', :search, '%') " +
                "     OR a.username LIKE CONCAT('%', :search, '%')) " +
                "ORDER BY u.full_name ASC",
        resultSetMapping = "CustomerSearchMapping"
)
@SqlResultSetMapping(
        name = "CustomerSearchMapping",
        classes = @ConstructorResult(
                targetClass = UserResponseDto.class,
                columns = {
                        @ColumnResult(name = "userId", type = Long.class),
                        @ColumnResult(name = "accountId", type = Long.class),
                        @ColumnResult(name = "username", type = String.class),
                        @ColumnResult(name = "email", type = String.class),
                        @ColumnResult(name = "phone", type = String.class),
                        @ColumnResult(name = "fullName", type = String.class),
                        @ColumnResult(name = "role", type = String.class),
                        @ColumnResult(name = "status", type = String.class)
                }
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false, unique = true)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", referencedColumnName = "id")
    private Branch branch;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải từ 10 đến 11 chữ số")
    @Column(name = "phone", length = 20)
    private String phone;

    @NotBlank(message = "Họ và tên không được để trống")
    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "image_url", length = 50)
    private String imageUrl;

    @Column(name = "address", length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private AccountStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<BorrowSlip> customerBorrowSlips = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "staff", fetch = FetchType.LAZY)
    private List<BorrowSlip> staffBorrowSlips = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();
}
