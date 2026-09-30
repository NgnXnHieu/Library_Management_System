package com.library.requestform.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Form tiếp nhận các tham số lọc tìm kiếm người dùng (User).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFilterRequestForm {

    /**
     * Từ khóa tìm kiếm chung (tìm kiếm trong: username, fullName, phone, email)
     */
    private String search;
}
