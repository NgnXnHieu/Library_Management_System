package com.library.dto.book;

import com.library.enums.DisplayStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin chi tiết đầu sách.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponseDto {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private Integer publicationYear;
    private String description;
    private String coverImageKey;
    private String coverImageUrl;
    private BigDecimal price;
    private BigDecimal rentalPrice;
    private BigDecimal fineAmount;
    private DisplayStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
