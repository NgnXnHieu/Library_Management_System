package com.library.entity;

import com.library.dto.book.BookDetailCustomerResponseDto;
import com.library.enums.DisplayStatus;
import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@NamedNativeQuery(
        name = "Book.findBookDetailCustomerById",
        query = "SELECT b.id AS id, b.isbn AS isbn, b.title AS title, b.author AS author, " +
                "b.publisher AS publisher, b.publication_year AS publicationYear, " +
                "b.description AS description, b.cover_image_key AS coverImageKey, " +
                "b.price AS price, b.rental_price AS rentalPrice, b.fine_amount AS fineAmount, " +
                "b.status AS status, c.id AS categoryId, c.name AS categoryName " +
                "FROM books b " +
                "JOIN categories c ON b.category_id = c.id " +
                "WHERE b.id = :bookId",
        resultSetMapping = "BookDetailCustomerMapping"
)
@SqlResultSetMapping(
        name = "BookDetailCustomerMapping",
        classes = @ConstructorResult(
                targetClass = BookDetailCustomerResponseDto.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "isbn", type = String.class),
                        @ColumnResult(name = "title", type = String.class),
                        @ColumnResult(name = "author", type = String.class),
                        @ColumnResult(name = "publisher", type = String.class),
                        @ColumnResult(name = "publicationYear", type = Integer.class),
                        @ColumnResult(name = "description", type = String.class),
                        @ColumnResult(name = "coverImageKey", type = String.class),
                        @ColumnResult(name = "price", type = BigDecimal.class),
                        @ColumnResult(name = "rentalPrice", type = BigDecimal.class),
                        @ColumnResult(name = "fineAmount", type = BigDecimal.class),
                        @ColumnResult(name = "status", type = String.class),
                        @ColumnResult(name = "categoryId", type = Long.class),
                        @ColumnResult(name = "categoryName", type = String.class)
                }
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "books")
public class Book extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
    private Category category;

    @Column(name = "isbn", length = 20, nullable = false, unique = true)
    private String isbn;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "author", length = 150)
    private String author;

    @Column(name = "publisher", length = 150)
    private String publisher;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image_key", length = 255)
    private String coverImageKey;

    @Column(name = "price", precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "rental_price", precision = 15, scale = 2)
    private BigDecimal rentalPrice;

    @Column(name = "fine_amount", precision = 15, scale = 2)
    private BigDecimal fineAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private DisplayStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<Inventory> inventories = new ArrayList<>();
}
