package com.library.service;

import com.library.dto.user.UserResponseDto;
import com.library.requestform.user.CustomerFilterRequestForm;
import com.library.requestform.user.UserAdminFilterRequestForm;
import com.library.requestform.user.UserFilterRequestForm;
import com.library.requestform.user.UserUpdateRequestForm;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {

    UserResponseDto getUserById(Long id);

    UserResponseDto getUserByEmail(String email);

    List<UserResponseDto> getAllUsers();

    UserResponseDto updateUser(Long id, UserUpdateRequestForm form);

    void changeUserStatus(Long id, String status);

    void deleteUser(Long id);

    /**
     * Lấy danh sách người dùng theo bộ lọc tìm kiếm và sắp xếp cũ đến mới theo createdAt.
     * Sử dụng JPA Specification và JOIN FETCH account để tối ưu hiệu năng.
     *
     * @param filter Bộ lọc tìm kiếm người dùng (username, fullName, phone, email)
     * @return Danh sách DTO người dùng thỏa mãn điều kiện
     */
    List<UserResponseDto> filterUsers(UserFilterRequestForm filter);

    /**
     * Lấy danh sách phân trang người dùng kèm theo bộ lọc mở rộng và sắp xếp (Dành riêng cho ADMIN).
     *
     * @param filter Bộ lọc tìm kiếm và thông tin phân trang người dùng
     * @return Trang kết quả chứa danh sách UserResponseDto
     */
    Page<UserResponseDto> getUsersWithFilter(UserAdminFilterRequestForm filter);

    /**
     * Lấy danh sách độc giả (role CUSTOMER, status ACTIVE) kèm tìm kiếm phục vụ lập phiếu mượn.
     * Áp đặt nghiệp vụ role CUSTOMER và status ACTIVE tại tầng Service.
     *
     * @param filter Bộ lọc chứa từ khóa tìm kiếm (search: username, fullName, phone, email)
     * @return Danh sách DTO độc giả thỏa mãn
     */
    List<UserResponseDto> filterCustomers(UserFilterRequestForm filter);

    /**
     * Lấy danh sách phân trang người dùng có vai trò là khách hàng (CUSTOMER) kèm các tiêu chí lọc:
     * username, fullName, phone, email, status và phân trang, sắp xếp.
     *
     * @param filter Form chứa các tiêu chí lọc và phân trang khách hàng
     * @return Trang kết quả chứa danh sách UserResponseDto
     */
    Page<UserResponseDto> getCustomersWithFilter(CustomerFilterRequestForm filter);
}
