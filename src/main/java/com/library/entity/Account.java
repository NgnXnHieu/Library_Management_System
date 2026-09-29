package com.library.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "accounts")
public class Account extends BaseEntity {

    @NotBlank(message = "Tài khoản không được để trống")
    @Size(min = 10, max = 50, message = "Tài khoản phải từ 10 đến 50 ký tự")
    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private User user;
}
