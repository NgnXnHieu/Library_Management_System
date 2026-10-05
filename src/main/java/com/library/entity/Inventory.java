package com.library.entity;

import com.library.dto.book.BookBranchInventoryDto;
import com.library.dto.inventory.InventoryResponseDto;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể quản lý tồn kho sách tại từng chi nhánh thư viện.
 */
@NamedNativeQuery(
        name = "Inventory.findCustomerInventoriesByBookAndBranch",
        query = "SELECT i.id AS inventoryId, br.id AS branchId, br.name AS branchName, " +
                "br.address AS branchAddress, br.phone AS branchPhone, " +
                "i.total_quantity AS totalQuantity, i.available_quantity AS availableQuantity, " +
                "i.shelf_location AS shelfLocation, i.status AS status " +
                "FROM inventories i " +
                "JOIN branches br ON i.branch_id = br.id " +
                "WHERE i.book_id = :bookId AND i.branch_id = :branchId " +
                "AND i.status = 'UNHIDE' AND br.status = 'OPEN'",
        resultSetMapping = "BookBranchInventoryMapping"
)
@NamedNativeQuery(
        name = "Inventory.findAllCustomerInventoriesByBook",
        query = "SELECT i.id AS inventoryId, br.id AS branchId, br.name AS branchName, " +
                "br.address AS branchAddress, br.phone AS branchPhone, " +
                "i.total_quantity AS totalQuantity, i.available_quantity AS availableQuantity, " +
                "i.shelf_location AS shelfLocation, i.status AS status " +
                "FROM inventories i " +
                "JOIN branches br ON i.branch_id = br.id " +
                "WHERE i.book_id = :bookId " +
                "AND i.status = 'UNHIDE' AND br.status = 'OPEN' " +
                "ORDER BY br.name ASC",
        resultSetMapping = "BookBranchInventoryMapping"
)
@SqlResultSetMapping(
        name = "BookBranchInventoryMapping",
        classes = @ConstructorResult(
                targetClass = BookBranchInventoryDto.class,
                columns = {
                        @ColumnResult(name = "inventoryId", type = Long.class),
                        @ColumnResult(name = "branchId", type = Long.class),
                        @ColumnResult(name = "branchName", type = String.class),
                        @ColumnResult(name = "branchAddress", type = String.class),
                        @ColumnResult(name = "branchPhone", type = String.class),
                        @ColumnResult(name = "totalQuantity", type = Integer.class),
                        @ColumnResult(name = "availableQuantity", type = Integer.class),
                        @ColumnResult(name = "shelfLocation", type = String.class),
                        @ColumnResult(name = "status", type = String.class)
                }
        )
)
@SqlResultSetMapping(
        name = "InventoryPageResponseMapping",
        classes = @ConstructorResult(
                targetClass = InventoryResponseDto.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "branchId", type = Long.class),
                        @ColumnResult(name = "categoryId", type = Long.class),
                        @ColumnResult(name = "categoryName", type = String.class),
                        @ColumnResult(name = "branchName", type = String.class),
                        @ColumnResult(name = "bookId", type = Long.class),
                        @ColumnResult(name = "bookTitle", type = String.class),
                        @ColumnResult(name = "isbn", type = String.class),
                        @ColumnResult(name = "author", type = String.class),
                        @ColumnResult(name = "publisher", type = String.class),
                        @ColumnResult(name = "publicationYear", type = Integer.class),
                        @ColumnResult(name = "coverImageUrl", type = String.class),
                        @ColumnResult(name = "price", type = BigDecimal.class),
                        @ColumnResult(name = "rentalPrice", type = BigDecimal.class),
                        @ColumnResult(name = "fineAmount", type = BigDecimal.class),
                        @ColumnResult(name = "totalQuantity", type = Integer.class),
                        @ColumnResult(name = "availableQuantity", type = Integer.class),
                        @ColumnResult(name = "status", type = String.class),
                        @ColumnResult(name = "shelfLocation", type = String.class),
                        @ColumnResult(name = "createdAt", type = LocalDateTime.class),
                        @ColumnResult(name = "updatedAt", type = LocalDateTime.class)
                }
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(
        name = "inventories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_inventory_branch_book", columnNames = {"branch_id", "book_id"})
        }
)
public class Inventory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", referencedColumnName = "id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", referencedColumnName = "id", nullable = false)
    private Book book;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "available_quantity")
    private Integer availableQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private DisplayStatus status;

    @Column(name = "shelf_location", length = 100)
    private String shelfLocation;

    @Builder.Default
    @OneToMany(mappedBy = "inventory", fetch = FetchType.LAZY)
    private List<BorrowItem> borrowItems = new ArrayList<>();
}
