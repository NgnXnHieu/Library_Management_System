package com.library.service.impl;

import com.library.dto.user.UserResponseDto;
import com.library.entity.Branch;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.enums.AccountStatus;
import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.UserMapper;
import com.library.repository.BranchRepository;
import com.library.repository.RoleRepository;
import com.library.repository.UserRepository;
import com.library.requestform.user.UserAdminFilterRequestForm;
import com.library.requestform.user.UserFilterRequestForm;
import com.library.requestform.user.UserUpdateRequestForm;
import com.library.service.UserService;
import com.library.specification.UserSpecification;
import com.library.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + email));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toDtoList(users);
    }

    /**
     * Cập nhật thông tin tài khoản người dùng theo ma trận phân quyền nghiệp vụ:
     * - ADMIN: Cập nhật thông tin cho tất cả các role trừ ADMIN.
     * - BRANCHMANAGER: Cập nhật thông tin cho tất cả các role trừ ADMIN và BRANCHMANAGER.
     *   Có thể đổi role tài khoản nhưng không được đổi lên ADMIN.
     * - STAFF: Chỉ được cập nhật thông tin tài khoản CUSTOMER.
     * - Quy tắc vai trò:
     *   + Tài khoản có role là CUSTOMER thì không được phép đổi sang role khác.
     *   + Tài khoản có role khác CUSTOMER thì không được phép hạ xuống role CUSTOMER.
     *
     * @param id   ID người dùng cần cập nhật
     * @param form Dữ liệu cập nhật
     * @return DTO người dùng sau khi cập nhật
     */
    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateRequestForm form) {
        // Bước 1: Tìm người dùng cần cập nhật theo ID
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));

        // Bước 2: Xác định vai trò của người thực hiện (Current User) và người được sửa (Target User)
        String currentUserRole = SecurityUtil.getCurrentRoleCode()
                .map(r -> r.replace("ROLE_", "").toUpperCase())
                .orElse("");

        String targetUserRole = (user.getRole() != null && user.getRole().getCode() != null)
                ? user.getRole().getCode().replace("ROLE_", "").toUpperCase()
                : "";

        // Bước 3: Kiểm tra quyền hạn truy cập theo ma trận vai trò (Business Rules Matrix)
        // 3.1. ADMIN không được phép sửa tài khoản của ADMIN khác
        if ("ADMIN".equals(currentUserRole) && "ADMIN".equals(targetUserRole)) {
            throw new BadRequestException("Quản trị viên không được phép thay đổi thông tin của tài khoản Quản trị viên (Admin) khác!");
        }

        // 3.2. BRANCHMANAGER không được phép sửa tài khoản ADMIN và BRANCHMANAGER khác
        if ("BRANCHMANAGER".equals(currentUserRole) && ("ADMIN".equals(targetUserRole) || "BRANCHMANAGER".equals(targetUserRole))) {
            throw new BadRequestException("Quản lý chi nhánh không có quyền thay đổi thông tin của tài khoản Admin hoặc Quản lý chi nhánh khác!");
        }

        // 3.3. STAFF chỉ được phép cập nhật tài khoản CUSTOMER
        if ("STAFF".equals(currentUserRole) && !"CUSTOMER".equals(targetUserRole)) {
            throw new BadRequestException("Nhân viên chỉ có quyền cập nhật thông tin tài khoản Khách hàng (Customer)!");
        }

        // Bước 4: Kiểm tra ràng buộc khi thay đổi vai trò (Role)
        if (form.getRole() != null && !form.getRole().trim().isEmpty()) {
            String newRoleCode = form.getRole().trim().replace("ROLE_", "").toUpperCase();

            // 4.1. Tài khoản có vai trò là CUSTOMER thì tuyệt đối không được phép đổi role
            if ("CUSTOMER".equals(targetUserRole) && !newRoleCode.equals("CUSTOMER")) {
                throw new BadRequestException("Tài khoản có vai trò Khách hàng (Customer) không được phép thay đổi sang vai trò khác!");
            }

            // 4.2. Tài khoản có vai trò khác CUSTOMER thì không được phép hạ xuống vai trò CUSTOMER
            if (!"CUSTOMER".equals(targetUserRole) && "CUSTOMER".equals(newRoleCode)) {
                throw new BadRequestException("Tài khoản có vai trò quản lý/nhân sự không được phép chuyển đổi xuống vai trò Khách hàng (Customer)!");
            }

            // 4.3. BRANCHMANAGER không được phép nâng quyền tài khoản lên ADMIN
            if ("BRANCHMANAGER".equals(currentUserRole) && "ADMIN".equals(newRoleCode)) {
                throw new BadRequestException("Quản lý chi nhánh không được phép nâng quyền tài khoản lên Quản trị viên (Admin)!");
            }

            // 4.4. Nếu role mới khác role hiện tại, tìm và cập nhật Role entity
            if (!newRoleCode.equals(targetUserRole)) {
                Role newRole = roleRepository.findByCode(newRoleCode)
                        .or(() -> roleRepository.findByCode("ROLE_" + newRoleCode))
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò: " + newRoleCode));
                user.setRole(newRole);
            }
        }

        // Bước 5: Kiểm tra và cập nhật chi nhánh nếu có truyền branchId
        if (form.getBranchId() != null) {
            Branch branch = branchRepository.findById(form.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + form.getBranchId()));
            user.setBranch(branch);
        }

        // Bước 6: Kiểm tra trùng email với người dùng khác
        if (form.getEmail() != null && !form.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(form.getEmail(), id)) {
                throw new BadRequestException("Email '" + form.getEmail() + "' đã được sử dụng bởi người dùng khác!");
            }
        }

        // Bước 7: Kiểm tra trùng số điện thoại với người dùng khác
        if (form.getPhone() != null && !form.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhoneAndIdNot(form.getPhone(), id)) {
                throw new BadRequestException("Số điện thoại '" + form.getPhone() + "' đã được sử dụng bởi người dùng khác!");
            }
        }

        // Bước 8: Ánh xạ các thông tin còn lại từ form sang entity (họ tên, ngày sinh, địa chỉ, status...)
        userMapper.updateEntityFromForm(form, user);

        // Lưu thông tin người dùng đã cập nhật
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public void changeUserStatus(Long id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        user.setStatus(status != null ? AccountStatus.valueOf(status.trim().toUpperCase()) : null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        userRepository.delete(user);
    }

    /**
     * Lấy danh sách người dùng theo bộ lọc tìm kiếm và sắp xếp cũ đến mới theo createdAt.
     * Sử dụng JPA Specification và JOIN FETCH account để tối ưu hiệu năng và tránh N+1 query.
     *
     * @param filter Bộ lọc tìm kiếm người dùng (username, fullName, phone, email)
     * @return Danh sách DTO người dùng thỏa mãn điều kiện
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> filterUsers(UserFilterRequestForm filter) {
        // Bước 1: Xây dựng Specification truy vấn lọc kết hợp JOIN FETCH account
        Specification<User> spec = UserSpecification.filter(filter);

        // Bước 2: Thiết lập sắp xếp theo thứ tự cũ đến mới của created_at (createdAt ASC)
        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");

        // Bước 3: Thực hiện truy vấn danh sách người dùng từ Database
        List<User> users = userRepository.findAll(spec, sort);

        // Bước 4: Chuyển đổi danh sách User entity sang danh sách UserResponseDto bằng MapStruct
        return userMapper.toDtoList(users);
    }

    /**
     * Lấy danh sách phân trang người dùng kèm theo bộ lọc mở rộng và sắp xếp (Dành riêng cho ADMIN).
     *
     * @param filter Bộ lọc tìm kiếm và thông tin phân trang người dùng
     * @return Trang kết quả chứa danh sách UserResponseDto
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getUsersWithFilter(UserAdminFilterRequestForm filter) {
        // Bước 1: Khởi tạo Specification từ filter form (đã bao gồm JOIN FETCH account, branch, role)
        Specification<User> spec = UserSpecification.filterAdmin(filter);

        // Bước 2: Xác định hướng sắp xếp (mặc định DESC - mới nhất lên đầu)
        Sort.Direction direction = (filter.getSortDir() != null && "asc".equalsIgnoreCase(filter.getSortDir()))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        // Bước 3: Xác định trường sắp xếp (chuẩn hóa thuộc tính cho trường hợp liên kết)
        String sortByProperty = "createdAt";
        if (filter.getSortBy() != null && !filter.getSortBy().trim().isEmpty()) {
            String rawSort = filter.getSortBy().trim();
            if ("username".equalsIgnoreCase(rawSort)) {
                sortByProperty = "account.username";
            } else if ("created_at".equalsIgnoreCase(rawSort) || "createdAt".equalsIgnoreCase(rawSort)) {
                sortByProperty = "createdAt";
            } else if ("phone".equalsIgnoreCase(rawSort)) {
                sortByProperty = "phone";
            } else if ("email".equalsIgnoreCase(rawSort)) {
                sortByProperty = "email";
            } else {
                sortByProperty = rawSort;
            }
        }
        Sort sort = Sort.by(direction, sortByProperty);

        // Bước 4: Khởi tạo đối tượng phân trang Pageable
        int page = Math.max(filter.getPage(), 0);
        int size = filter.getSize() > 0 ? filter.getSize() : 10;
        Pageable pageable = PageRequest.of(page, size, sort);

        // Bước 5: Thực hiện truy vấn kết hợp Specification và phân trang
        Page<User> userPage = userRepository.findAll(spec, pageable);

        // Bước 6: Ánh xạ kết quả sang DTO bằng MapStruct
        return userPage.map(userMapper::toDto);
    }

    /**
     * Lấy danh sách độc giả (role CUSTOMER, status ACTIVE) phục vụ tìm kiếm lập phiếu mượn.
     * Áp đặt nghiệp vụ role CUSTOMER và status ACTIVE tại tầng Service, truyền vào Specification động.
     *
     * @param filter Bộ lọc chứa từ khóa tìm kiếm (search: username, fullName, phone, email)
     * @return Danh sách DTO người dùng thỏa mãn
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> filterCustomers(UserFilterRequestForm filter) {
        // Bước 1: Khởi tạo filter nếu null và thiết lập nghiệp vụ role CUSTOMER, status ACTIVE
        if (filter == null) {
            filter = new UserFilterRequestForm();
        }
        filter.setRole("CUSTOMER");
        filter.setStatus(AccountStatus.ACTIVE);

        // Bước 2: Tạo Specification động từ filter
        Specification<User> spec = UserSpecification.filter(filter);

        // Bước 3: Sắp xếp theo họ tên độc giả tăng dần (fullName ASC)
        Sort sort = Sort.by(Sort.Direction.ASC, "fullName");

        // Bước 4: Truy vấn cơ sở dữ liệu
        List<User> users = userRepository.findAll(spec, sort);

        // Bước 5: Chuyển đổi sang danh sách DTO qua MapStruct
        return userMapper.toDtoList(users);
    }
}
