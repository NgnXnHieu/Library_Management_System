package com.library.dto.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO chứa thông tin tồn kho của sách tại một chi nhánh cụ thể.
 * Được ánh xạ trực tiếp từ câu lệnh Native SQL thông qua @SqlResultSetMapping và @ConstructorResult.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookBranchInventoryDto {

    private Long inventoryId;
    private Long branchId;
    private String branchName;
    private String branchAddress;
    private String branchPhone;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private String shelfLocation;
    private String status;
}
