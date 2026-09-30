package com.library.service.impl;

import com.library.dto.user.UserResponseDto;
import com.library.entity.User;
import com.library.enums.AccountStatus;
import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.UserMapper;
import com.library.repository.UserRepository;
import com.library.requestform.user.UserFilterRequestForm;
import com.library.requestform.user.UserUpdateRequestForm;
import com.library.service.UserService;
import com.library.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
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

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateRequestForm form) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));

        // Nghiệp vụ: Kiểm tra trùng email với người dùng khác
        if (form.getEmail() != null && !form.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(form.getEmail(), id)) {
                throw new BadRequestException("Email '" + form.getEmail() + "' đã được sử dụng bởi người dùng khác!");
            }
        }

        // Nghiệp vụ: Kiểm tra trùng số điện thoại với người dùng khác
        if (form.getPhone() != null && !form.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhoneAndIdNot(form.getPhone(), id)) {
                throw new BadRequestException("Số điện thoại '" + form.getPhone() + "' đã được sử dụng bởi người dùng khác!");
            }
        }

        userMapper.updateEntityFromForm(form, user);
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
}
