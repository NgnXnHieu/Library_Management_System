package com.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "branches")
public class Branch extends BaseEntity {

    @Column(name = "code", length = 20, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "status", length = 20)
    private String status;

    @Builder.Default
    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    private List<BranchBook> branchBooks = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    private List<BorrowSlip> borrowSlips = new ArrayList<>();
}
