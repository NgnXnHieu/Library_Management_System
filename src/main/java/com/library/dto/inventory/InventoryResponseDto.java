package com.library.dto.inventory;

import com.library.enums.DisplayStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin chi tiết của một bản ghi tồn kho sách tại chi nhánh.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponseDto {
    private Long id;
    private Long branchId;
    private Long categoryId;
    private String categoryName;
    private String branchName;
    private Long bookId;
    private String bookTitle;
    private String isbn;
    private String coverImageUrl;
    private BigDecimal price;
    private BigDecimal rentalPrice;
    private BigDecimal fineAmount;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private DisplayStatus status;
    private String shelfLocation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
