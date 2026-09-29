package com.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @Column(name = "status", length = 20)
    private String status;

    @Builder.Default
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<BranchBook> branchBooks = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<BorrowItem> borrowItems = new ArrayList<>();
}
