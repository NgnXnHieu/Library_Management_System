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
    private String author;
    private String publisher;
    private Integer publicationYear;
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

    /**
     * Constructor phục vụ ánh xạ trực tiếp từ Native SQL Query qua @SqlResultSetMapping "InventoryPageResponseMapping".
     */
    public InventoryResponseDto(
            Long id,
            Long branchId,
            Long categoryId,
            String categoryName,
            String branchName,
            Long bookId,
            String bookTitle,
            String isbn,
            String author,
            String publisher,
            Integer publicationYear,
            String coverImageUrl,
            BigDecimal price,
            BigDecimal rentalPrice,
            BigDecimal fineAmount,
            Integer totalQuantity,
            Integer availableQuantity,
            String status,
            String shelfLocation,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.branchId = branchId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.branchName = branchName;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.isbn = isbn;
        this.author = author;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.coverImageUrl = coverImageUrl;
        this.price = price;
        this.rentalPrice = rentalPrice;
        this.fineAmount = fineAmount;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.status = status != null ? DisplayStatus.valueOf(status.trim().toUpperCase()) : null;
        this.shelfLocation = shelfLocation;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Overload constructor hỗ trợ kiểu java.sql.Timestamp từ JDBC Driver.
     */
    public InventoryResponseDto(
            Long id,
            Long branchId,
            Long categoryId,
            String categoryName,
            String branchName,
            Long bookId,
            String bookTitle,
            String isbn,
            String author,
            String publisher,
            Integer publicationYear,
            String coverImageUrl,
            BigDecimal price,
            BigDecimal rentalPrice,
            BigDecimal fineAmount,
            Integer totalQuantity,
            Integer availableQuantity,
            String status,
            String shelfLocation,
            java.sql.Timestamp createdAt,
            java.sql.Timestamp updatedAt) {
        this(id, branchId, categoryId, categoryName, branchName, bookId, bookTitle, isbn,
             author, publisher, publicationYear, coverImageUrl, price, rentalPrice, fineAmount,
             totalQuantity, availableQuantity, status, shelfLocation,
             createdAt != null ? createdAt.toLocalDateTime() : null,
             updatedAt != null ? updatedAt.toLocalDateTime() : null);
    }
}
