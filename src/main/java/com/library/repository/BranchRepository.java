package com.library.repository;

import com.library.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository thao tác với bảng branches trong Database.
 * Kế thừa JpaSpecificationExecutor để hỗ trợ lọc động qua Specification.
 */
@Repository
public interface BranchRepository extends JpaRepository<Branch, Long>, JpaSpecificationExecutor<Branch> {

    Optional<Branch> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}

