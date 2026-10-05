package com.library.repository.impl;

import com.library.dto.inventory.InventoryResponseDto;
import com.library.repository.InventoryRepositoryCustom;
import com.library.requestform.inventory.InventoryFilterRequestForm;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Triển khai các phương thức truy vấn tùy biến cho InventoryRepository sử dụng Native SQL.
 */
@Repository
@RequiredArgsConstructor
public class InventoryRepositoryCustomImpl implements InventoryRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Page<InventoryResponseDto> findAllInventoriesNative(InventoryFilterRequestForm filter, Pageable pageable) {
        // Bước 1: Khởi tạo mệnh đề SELECT cơ bản
        StringBuilder selectClause = new StringBuilder(
                "SELECT i.id AS id, br.id AS branchId, c.id AS categoryId, c.name AS categoryName, " +
                "br.name AS branchName, b.id AS bookId, b.title AS bookTitle, b.isbn AS isbn, " +
                "b.author AS author, b.publisher AS publisher, b.publication_year AS publicationYear, " +
                "b.cover_image_key AS coverImageUrl, b.price AS price, b.rental_price AS rentalPrice, " +
                "b.fine_amount AS fineAmount, i.total_quantity AS totalQuantity, " +
                "i.available_quantity AS availableQuantity, i.status AS status, " +
                "i.shelf_location AS shelfLocation, i.created_at AS createdAt, i.updated_at AS updatedAt "
        );

        StringBuilder fromClause = new StringBuilder(
                "FROM inventories i " +
                "JOIN branches br ON i.branch_id = br.id " +
                "JOIN books b ON i.book_id = b.id " +
                "LEFT JOIN categories c ON b.category_id = c.id " +
                "WHERE 1=1 "
        );

        Map<String, Object> params = new HashMap<>();

        // Bước 2: Ghép các tiêu chí lọc động từ InventoryFilterRequestForm
        if (filter != null) {
            if (filter.getBranchId() != null) {
                fromClause.append("AND i.branch_id = :branchId ");
                params.put("branchId", filter.getBranchId());
            }

            if (filter.getCategoryId() != null) {
                fromClause.append("AND b.category_id = :categoryId ");
                params.put("categoryId", filter.getCategoryId());
            }

            if (filter.getBookTitle() != null && !filter.getBookTitle().trim().isEmpty()) {
                fromClause.append("AND b.title LIKE :bookTitle ");
                params.put("bookTitle", "%" + filter.getBookTitle().trim() + "%");
            }

            if (filter.getMinPrice() != null) {
                fromClause.append("AND b.price >= :minPrice ");
                params.put("minPrice", filter.getMinPrice());
            }

            if (filter.getMaxPrice() != null) {
                fromClause.append("AND b.price <= :maxPrice ");
                params.put("maxPrice", filter.getMaxPrice());
            }

            if (filter.getMinRentalPrice() != null) {
                fromClause.append("AND b.rental_price >= :minRentalPrice ");
                params.put("minRentalPrice", filter.getMinRentalPrice());
            }

            if (filter.getMaxRentalPrice() != null) {
                fromClause.append("AND b.rental_price <= :maxRentalPrice ");
                params.put("maxRentalPrice", filter.getMaxRentalPrice());
            }

            if (filter.getMinFineAmount() != null) {
                fromClause.append("AND b.fine_amount >= :minFineAmount ");
                params.put("minFineAmount", filter.getMinFineAmount());
            }

            if (filter.getMaxFineAmount() != null) {
                fromClause.append("AND b.fine_amount <= :maxFineAmount ");
                params.put("maxFineAmount", filter.getMaxFineAmount());
            }

            if (filter.getStatus() != null) {
                fromClause.append("AND i.status = :status ");
                params.put("status", filter.getStatus().name());
            }
        }

        // Bước 3: Thực hiện truy vấn đếm tổng số bản ghi (COUNT query)
        String countSql = "SELECT COUNT(*) " + fromClause.toString();
        Query countQuery = entityManager.createNativeQuery(countSql);
        params.forEach(countQuery::setParameter);

        long total = ((Number) countQuery.getSingleResult()).longValue();
        if (total == 0) {
            return Page.empty(pageable);
        }

        // Bước 4: Xử lý sắp xếp an toàn tránh SQL Injection
        String rawSortBy = (filter != null && filter.getSortBy() != null && !filter.getSortBy().trim().isEmpty())
                ? filter.getSortBy().trim()
                : "updatedAt";

        String sortColumn = switch (rawSortBy) {
            case "bookTitle" -> "b.title";
            case "price" -> "b.price";
            case "rentalPrice" -> "b.rental_price";
            case "fineAmount" -> "b.fine_amount";
            case "categoryName" -> "c.name";
            case "branchName" -> "br.name";
            case "totalQuantity" -> "i.total_quantity";
            case "availableQuantity" -> "i.available_quantity";
            case "createdAt" -> "i.created_at";
            case "status" -> "i.status";
            default -> "i.updated_at";
        };

        String sortDirection = (filter != null && "asc".equalsIgnoreCase(filter.getSortDir()))
                ? "ASC"
                : "DESC";

        String dataSql = selectClause.toString() + fromClause.toString() + "ORDER BY " + sortColumn + " " + sortDirection;

        // Bước 5: Thực hiện truy vấn phân trang lấy danh sách DTO qua @SqlResultSetMapping "InventoryPageResponseMapping"
        Query dataQuery = entityManager.createNativeQuery(dataSql, "InventoryPageResponseMapping");
        params.forEach(dataQuery::setParameter);

        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<InventoryResponseDto> content = dataQuery.getResultList();

        return new PageImpl<>(content, pageable, total);
    }
}
