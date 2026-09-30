package com.library.dto.branch;

import com.library.enums.BranchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin chi nhánh thư viện.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchResponseDto {

    private Long id;
    private String code;
    private String name;
    private String address;
    private String phone;
    private BranchStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
