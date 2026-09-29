package com.library.repository;

import com.library.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * Tìm vai trò mặc định khi đăng ký tài khoản (ưu tiên CUSTOMER, sau đó READER)
     */
    default Optional<Role> findDefaultRole() {
        return findByCode("CUSTOMER").or(() -> findByCode("READER"));
    }
}
