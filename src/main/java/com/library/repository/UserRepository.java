package com.library.repository;

import com.library.entity.User;
import com.library.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    boolean existsByRoleId(Long roleId);

    Optional<User> findByAccountId(Long accountId);

    @Query("SELECT u FROM User u " +
           "JOIN FETCH u.account a " +
           "JOIN FETCH u.role r " +
           "LEFT JOIN FETCH u.branch b " +
           "WHERE a.id = :accountId")
    Optional<User> findByAccountIdWithDetails(@Param("accountId") Long accountId);

    List<User> findByStatus(AccountStatus status);
}

