package com.library.dto.borrow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO trả về thông tin chi tiết một cuốn sách trong phiếu mượn.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowItemResponseDto {

    private Long id;
    private Long inventoryId;
    private Long bookId;
    private String bookTitle;
    private String isbn;
    private BigDecimal rentalPrice;
    private Integer quantity;
}
