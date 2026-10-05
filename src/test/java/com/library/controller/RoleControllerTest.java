package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.role.RoleResponseDto;
import com.library.exception.AppException;
import com.library.exception.BadRequestException;
import com.library.exception.ErrorCode;
import com.library.requestform.role.RoleCreateRequestForm;
import com.library.requestform.role.RoleUpdateRequestForm;
import com.library.security.JwtAuthenticationFilter;
import com.library.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoleService roleService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testCreateRoleSuccess() throws Exception {
        RoleCreateRequestForm form = RoleCreateRequestForm.builder()
                .code("LIBRARIAN")
                .name("Thủ thư")
                .description("Quản lý sách và mượn trả")
                .build();

        RoleResponseDto responseDto = RoleResponseDto.builder()
                .id(1L)
                .code("LIBRARIAN")
                .name("Thủ thư")
                .description("Quản lý sách và mượn trả")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(roleService.createRole(any(RoleCreateRequestForm.class))).thenReturn(responseDto);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("LIBRARIAN"))
                .andExpect(jsonPath("$.data.name").value("Thủ thư"));
    }

    @Test
    void testCreateRoleValidationFailure() throws Exception {
        RoleCreateRequestForm form = RoleCreateRequestForm.builder()
                .code("") // Blank code
                .name("") // Blank name
                .build();

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.code").exists())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void testGetAllRoles() throws Exception {
        RoleResponseDto role1 = RoleResponseDto.builder().id(1L).code("ADMIN").name("Quản trị viên").build();
        RoleResponseDto role2 = RoleResponseDto.builder().id(2L).code("READER").name("Độc giả").build();

        when(roleService.getAllRoles()).thenReturn(List.of(role1, role2));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].code").value("ADMIN"))
                .andExpect(jsonPath("$.data[1].code").value("READER"));
    }

    @Test
    void testGetRoleByIdNotFound() throws Exception {
        when(roleService.getRoleById(99L))
                .thenThrow(new AppException(ErrorCode.ROLE_NOT_FOUND_BY_ID, 99L));

        mockMvc.perform(get("/roles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Không tìm thấy vai trò với ID: 99"));
    }

    @Test
    void testUpdateRoleSuccess() throws Exception {
        RoleUpdateRequestForm form = RoleUpdateRequestForm.builder()
                .code("SENIOR_LIBRARIAN")
                .name("Thủ thư cấp cao")
                .description("Quản lý toàn diện")
                .build();

        RoleResponseDto updatedDto = RoleResponseDto.builder()
                .id(1L)
                .code("SENIOR_LIBRARIAN")
                .name("Thủ thư cấp cao")
                .description("Quản lý toàn diện")
                .build();

        when(roleService.updateRole(eq(1L), any(RoleUpdateRequestForm.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("SENIOR_LIBRARIAN"));
    }

    @Test
    void testDeleteRoleSuccess() throws Exception {
        doNothing().when(roleService).deleteRole(1L);

        mockMvc.perform(delete("/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa vai trò thành công!"));
    }

    @Test
    void testDeleteRoleInUseThrowsBadRequest() throws Exception {
        doThrow(new BadRequestException("Không thể xóa vai trò vì đang có người dùng thuộc vai trò này!"))
                .when(roleService).deleteRole(1L);

        mockMvc.perform(delete("/roles/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Không thể xóa vai trò vì đang có người dùng thuộc vai trò này!"));
    }
}
