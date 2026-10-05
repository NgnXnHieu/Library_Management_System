package com.library.service;

import com.library.dto.category.CategoryResponseDto;
import com.library.entity.Category;
import com.library.enums.DisplayStatus;
import com.library.mapper.CategoryMapper;
import com.library.repository.BookRepository;
import com.library.repository.CategoryRepository;
import com.library.repository.InventoryRepository;
import com.library.requestform.category.CategoryUpdateRequestForm;
import com.library.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Test kiểm thử logic nghiệp vụ CategoryServiceImpl, bao gồm cập nhật trạng thái phân tầng sang Book và Inventory.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("Cập nhật thể loại sang trạng thái HIDE -> Tự động cập nhật tất cả sách và tồn kho liên quan sang HIDE")
    void testUpdateCategory_StatusHide_CascadesToBookAndInventory() {
        Category category = Category.builder()
                .name("Khoa học viễn tưởng")
                .status(DisplayStatus.UNHIDE)
                .build();
        category.setId(5L);

        CategoryUpdateRequestForm form = CategoryUpdateRequestForm.builder()
                .status(DisplayStatus.HIDE)
                .build();

        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        doAnswer(invocation -> {
            Category c = invocation.getArgument(1);
            c.setStatus(DisplayStatus.HIDE);
            return null;
        }).when(categoryMapper).updateEntityFromForm(eq(form), any(Category.class));

        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponseDto dto = CategoryResponseDto.builder()
                .id(5L)
                .name("Khoa học viễn tưởng")
                .status(DisplayStatus.HIDE)
                .build();
        when(categoryMapper.toDto(any(Category.class))).thenReturn(dto);

        CategoryResponseDto result = categoryService.updateCategory(5L, form);

        assertNotNull(result);
        assertEquals(DisplayStatus.HIDE, category.getStatus());
        verify(categoryRepository).save(category);
        verify(bookRepository).updateStatusByCategoryId(5L, DisplayStatus.HIDE);
        verify(inventoryRepository).updateStatusByBookCategoryId(5L, DisplayStatus.HIDE);
    }
}
