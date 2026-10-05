package com.library.dto.user;

import com.library.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private Long userId;
    private Long accountId;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private LocalDate dateOfBirth;
    private String imageUrl;
    private String address;
    private String role;
    private Long branchId;
    private String branchName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor phục vụ ánh xạ trực tiếp từ Native SQL Query qua @SqlResultSetMapping "CustomerSearchMapping".
     */
    public UserResponseDto(Long userId, Long accountId, String username, String email,
                           String phone, String fullName, String role, String status) {
        this.userId = userId;
        this.accountId = accountId;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
    }

    /**
     * Constructor phục vụ JPQL Constructor Expression (SELECT new com.library.dto.user.UserResponseDto(...)).
     * Ánh xạ trực tiếp từ JPQL Query lên DTO, tự động chuyển đổi AccountStatus enum sang String.
     */
    public UserResponseDto(Long userId, Long accountId, String username, String email,
                           String phone, String fullName, LocalDate dateOfBirth,
                           String imageUrl, String address, String role, Long branchId,
                           String branchName, AccountStatus status,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.accountId = accountId;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.imageUrl = imageUrl;
        this.address = address;
        this.role = role;
        this.branchId = branchId;
        this.branchName = branchName;
        this.status = status != null ? status.name() : null;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
