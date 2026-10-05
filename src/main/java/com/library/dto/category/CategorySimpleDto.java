package com.library.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO rút gọn cho thể loại sách (phục vụ menu dropdown và bộ lọc).
 * Chỉ bao gồm mã thể loại (id) và tên thể loại (name).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySimpleDto {

    private Long id;
    private String name;
}
