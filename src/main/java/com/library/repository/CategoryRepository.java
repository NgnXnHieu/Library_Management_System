package com.library.repository;

import com.library.dto.category.CategorySimpleDto;
import com.library.entity.Category;
import com.library.enums.DisplayStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác với bảng categories trong Database.
 * Kế thừa JpaSpecificationExecutor để hỗ trợ phân trang và lọc động qua JPA Criteria.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    /**
     * Lấy danh sách thể loại sách theo trạng thái hiển thị, sắp xếp tên A-Z.
     * Ánh xạ trực tiếp sang CategorySimpleDto qua Constructor Expression của JPQL.
     *
     * @param status Trạng thái hiển thị (UNHIDE)
     * @return Danh sách CategorySimpleDto
     */
    @Query("SELECT new com.library.dto.category.CategorySimpleDto(c.id, c.name) " +
           "FROM Category c " +
           "WHERE c.status = :status " +
           "ORDER BY c.name ASC")
    List<CategorySimpleDto> findActiveCategories(@Param("status") DisplayStatus status);

    /**
     * Tìm thể loại theo tên.
     *
     * @param name Tên thể loại
     * @return Optional chứa thể loại nếu tìm thấy
     */
    Optional<Category> findByName(String name);

    /**
     * Kiểm tra tên thể loại đã tồn tại trong DB chưa.
     *
     * @param name Tên thể loại
     * @return true nếu đã tồn tại, ngược lại false
     */
    boolean existsByName(String name);

    /**
     * Kiểm tra tên thể loại đã tồn tại ở bản ghi khác ngoại trừ ID hiện tại.
     *
     * @param name Tên thể loại cần kiểm tra
     * @param id   ID của thể loại đang được cập nhật
     * @return true nếu trùng với bản ghi khác, ngược lại false
     */
    boolean existsByNameAndIdNot(String name, Long id);
}
