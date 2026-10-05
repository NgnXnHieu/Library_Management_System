package com.library.dto.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO trả về thông tin chi tiết một đầu sách kèm danh sách tồn kho theo chi nhánh dành cho khách hàng.
 * Được ánh xạ qua @SqlResultSetMapping và @ConstructorResult.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDetailCustomerResponseDto {

    private Long id;
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
    private String status;
    private Long categoryId;
    private String categoryName;

    @Builder.Default
    private List<BookBranchInventoryDto> inventories = new ArrayList<>();

    /**
     * Constructor phục vụ ánh xạ trực tiếp từ Native SQL Query thông qua @ConstructorResult của JPA.
     */
    public BookDetailCustomerResponseDto(
            Long id,
            String isbn,
            String title,
            String author,
            String publisher,
            Integer publicationYear,
            String description,
            String coverImageKey,
            BigDecimal price,
            BigDecimal rentalPrice,
            BigDecimal fineAmount,
            String status,
            Long categoryId,
            String categoryName) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.description = description;
        this.coverImageKey = coverImageKey;
        this.price = price;
        this.rentalPrice = rentalPrice;
        this.fineAmount = fineAmount;
        this.status = status;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.inventories = new ArrayList<>();
    }
}
