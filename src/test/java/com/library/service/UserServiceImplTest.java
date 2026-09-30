package com.library.service;

import com.library.dto.user.UserResponseDto;
import com.library.entity.Account;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.enums.AccountStatus;
import com.library.mapper.UserMapper;
import com.library.repository.UserRepository;
import com.library.requestform.user.UserFilterRequestForm;
import com.library.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user1;
    private User user2;
    private Account account1;
    private Account account2;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        customerRole = Role.builder()
                .code("CUSTOMER")
                .name("Khách hàng")
                .build();
        customerRole.setId(1L);

        account1 = Account.builder()
                .username("nguyenvana")
                .passwordHash("hashedPass1")
                .status("ACTIVE")
                .build();
        account1.setId(101L);

        user1 = User.builder()
                .account(account1)
                .role(customerRole)
                .fullName("Nguyễn Văn A")
                .email("vana@example.com")
                .phone("0912345678")
                .status(AccountStatus.ACTIVE)
                .build();
        user1.setId(1L);
        user1.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));

        account2 = Account.builder()
                .username("tranthib")
                .passwordHash("hashedPass2")
                .status("ACTIVE")
                .build();
        account2.setId(102L);

        user2 = User.builder()
                .account(account2)
                .role(customerRole)
                .fullName("Trần Thị B")
                .email("thib@example.com")
                .phone("0987654321")
                .status(AccountStatus.ACTIVE)
                .build();
        user2.setId(2L);
        user2.setCreatedAt(LocalDateTime.of(2026, 2, 1, 10, 0));
    }

    @Test
    @DisplayName("Lọc danh sách người dùng thành công với Specification và sắp xếp createdAt ASC")
    @SuppressWarnings("unchecked")
    void testFilterUsers_Success() {
        // Arrange
        UserFilterRequestForm filter = UserFilterRequestForm.builder()
                .search("vana")
                .build();

        List<User> mockUsers = List.of(user1, user2);
        when(userRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(mockUsers);

        UserResponseDto dto1 = UserResponseDto.builder()
                .userId(1L)
                .accountId(101L)
                .username("nguyenvana")
                .fullName("Nguyễn Văn A")
                .email("vana@example.com")
                .phone("0912345678")
                .build();

        UserResponseDto dto2 = UserResponseDto.builder()
                .userId(2L)
                .accountId(102L)
                .username("tranthib")
                .fullName("Trần Thị B")
                .email("thib@example.com")
                .phone("0987654321")
                .build();

        when(userMapper.toDtoList(mockUsers)).thenReturn(List.of(dto1, dto2));

        // Act
        List<UserResponseDto> result = userService.filterUsers(filter);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("nguyenvana", result.get(0).getUsername());
        assertEquals("tranthib", result.get(1).getUsername());

        // Kiểm tra đối số Sort được truyền vào repository là createdAt ASC
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(userRepository).findAll(any(Specification.class), sortCaptor.capture());

        Sort capturedSort = sortCaptor.getValue();
        Sort.Order order = capturedSort.getOrderFor("createdAt");
        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }
}
