package com.library.requestform.user;

import com.library.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Form tiếp nhận các tham số tìm kiếm, lọc và phân trang danh sách người dùng dành riêng cho ADMIN.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAdminFilterRequestForm {

    /**
     * Từ khóa tìm kiếm áp dụng đồng thời cho: username (tên đăng nhập), email và phone (số điện thoại)
     */
    private String searchName;

    /**
     * Lọc theo vai trò (tìm kiếm theo mã code vai trò như ROLE_ADMIN, ROLE_STAFF... hoặc tên vai trò)
     */
    private String role;

    /**
     * Lọc theo tên chi nhánh trực thuộc của người dùng
     */
    private String branchName;

    /**
     * Danh sách trạng thái tài khoản cần lọc (ACTIVE, INACTIVE, LOCKED)
     */
    private List<AccountStatus> statuses;

    /**
     * Chỉ số trang hiện tại (0-indexed, mặc định 0)
     */
    @Builder.Default
    private int page = 0;

    /**
     * Số lượng bản ghi trên mỗi trang (mặc định 10)
     */
    @Builder.Default
    private int size = 10;

    /**
     * Trường dữ liệu dùng để sắp xếp (Hỗ trợ: createdAt, username, phone, email, fullName...). Mặc định "createdAt".
     */
    @Builder.Default
    private String sortBy = "createdAt";

    /**
     * Hướng sắp xếp: "desc" (mới nhất lên đầu) hoặc "asc" (cũ nhất lên đầu). Mặc định "desc".
     */
    @Builder.Default
    private String sortDir = "desc";
}
