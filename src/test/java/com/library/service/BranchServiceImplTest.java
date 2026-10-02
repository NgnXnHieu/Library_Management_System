package com.library.service;

import com.library.dto.branch.BranchResponseDto;
import com.library.entity.Branch;
import com.library.enums.BranchStatus;
import com.library.mapper.BranchMapper;
import com.library.repository.BranchRepository;
import com.library.repository.UserRepository;
import com.library.requestform.branch.BranchFilterRequestForm;
import com.library.service.impl.BranchServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchServiceImplTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BranchMapper branchMapper;

    @InjectMocks
    private BranchServiceImpl branchService;

    @Test
    @DisplayName("Kiểm tra phân trang chi nhánh với bộ lọc và sắp xếp mặc định createdAt DESC")
    void testGetBranchesWithFilter_DefaultSortCreatedAtDesc() {
        BranchFilterRequestForm filter = BranchFilterRequestForm.builder()
                .code("CN01")
                .name("Chi nhánh Hà Nội")
                .status(BranchStatus.OPEN)
                .build();

        Branch branch = Branch.builder()
                .code("CN01")
                .name("Chi nhánh Hà Nội")
                .status(BranchStatus.OPEN)
                .build();

        BranchResponseDto dto = BranchResponseDto.builder()
                .code("CN01")
                .name("Chi nhánh Hà Nội")
                .status(BranchStatus.OPEN)
                .build();

        Page<Branch> branchPage = new PageImpl<>(List.of(branch));
        when(branchRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(branchPage);
        when(branchMapper.toDto(branch)).thenReturn(dto);

        Page<BranchResponseDto> result = branchService.getBranchesWithFilter(filter);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("CN01", result.getContent().get(0).getCode());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(branchRepository).findAll(any(Specification.class), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        Sort.Order order = capturedPageable.getSort().getOrderFor("createdAt");
        assertNotNull(order, "Sắp xếp mặc định phải chứa trường createdAt");
        assertTrue(order.isDescending(), "Sắp xếp mặc định theo createdAt phải là DESC");
    }

    @Test
    @DisplayName("Kiểm tra lấy danh sách toàn bộ các giá trị enum BranchStatus")
    void testGetBranchStatuses() {
        List<BranchStatus> statuses = branchService.getBranchStatuses();

        assertNotNull(statuses);
        assertEquals(2, statuses.size());
        assertTrue(statuses.contains(BranchStatus.OPEN));
        assertTrue(statuses.contains(BranchStatus.CLOSED));
    }
}
