package com.library.controller;

import com.library.dto.category.CategorySimpleDto;
import com.library.security.JwtAuthenticationFilter;
import com.library.service.CategoryService;
import com.library.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test kiểm thử API công khai lấy danh sách thể loại active:
 * GET /public/categories/active
 */
@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerCustomerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtServiceImpl jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("Test: Lấy danh sách thể loại active thành công cho menu dropdown")
    void testGetActiveCategoriesSuccess() throws Exception {
        CategorySimpleDto cat1 = CategorySimpleDto.builder().id(1L).name("Công Nghệ Thông Tin").build();
        CategorySimpleDto cat2 = CategorySimpleDto.builder().id(2L).name("Kinh Tế & Khởi Nghiệp").build();
        CategorySimpleDto cat3 = CategorySimpleDto.builder().id(3L).name("Văn Học & Nghệ Thuật").build();

        when(categoryService.getActiveCategories()).thenReturn(List.of(cat1, cat2, cat3));

        mockMvc.perform(get("/public/categories/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lấy danh sách thể loại hiển thị thành công!"))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("Công Nghệ Thông Tin"))
                .andExpect(jsonPath("$.data[1].id").value(2L))
                .andExpect(jsonPath("$.data[1].name").value("Kinh Tế & Khởi Nghiệp"));
    }
}
