package com.library.dto.user;

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
}
