package com.library.security;

public final class SecurityConstants {

        private SecurityConstants() {
                // Utility class
        }

        public static final String[] PUBLIC_ENDPOINTS = {
                        "/public/**",
                        "/uploads/**",
                        "/auth/**",
                        "/hello/**",
                        "/error",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/v3/api-docs"
        };

        public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final String BEARER_PREFIX = "Bearer ";

        // =========================================================================
        // 2. ENDPOINTS DÀNH CHO ADMIN VÀ QUẢN LÝ CHI NHÁNH (ADMIN & BRANCHMANAGER)
        // =========================================================================
        public static final String[] ADMIN_AND_BM_PUT_ENDPOINTS = {
                        "/users/*/status"
        };

        // =========================================================================
        // 3. ENDPOINTS DÀNH RIÊNG CHO QUẢN TRỊ VIÊN HỆ THỐNG (ADMIN)
        // =========================================================================
        // Áp dụng cho mọi HTTP Method
        public static final String[] ADMIN_ANY_METHOD_ENDPOINTS = {
                        "/admin/**",
                        "/accounts/admin",
                        "/accounts/admin/**",
                        "/inventories/init/**"
        };

        // Thao tác Thêm mới dữ liệu (POST) dành riêng cho ADMIN
        public static final String[] ADMIN_POST_ENDPOINTS = {
                        "/books",
                        "/categories",
                        "/branches",
                        "/roles",
                        "/inventories/*/import"
        };

        // Thao tác Cập nhật dữ liệu (PUT) dành riêng cho ADMIN
        public static final String[] ADMIN_PUT_ENDPOINTS = {
                        "/books/*",
                        "/books/**",
                        "/categories/*",
                        "/categories/**",
                        "/branches/*",
                        "/branches/**",
                        "/roles/*",
                        "/roles/**",
                        "/inventories/*",
                        "/inventories/*/status"
        };

        // Thao tác Xóa dữ liệu (DELETE) dành riêng cho ADMIN
        public static final String[] ADMIN_DELETE_ENDPOINTS = {
                        "/books/*",
                        "/books/**",
                        "/categories/*",
                        "/categories/**",
                        "/branches/*",
                        "/branches/**",
                        "/roles/*",
                        "/roles/**"
        };

        // Thao tác Truy vấn danh sách tổng hợp (GET) dành riêng cho ADMIN
        public static final String[] ADMIN_GET_ENDPOINTS = {
                        "/inventories"
        };

        // =========================================================================
        // 4. ENDPOINTS CHO NHÂN SỰ TẠI CHI NHÁNH (BRANCHMANAGER & STAFF)
        // =========================================================================
        public static final String[] BRANCH_STAFF_POST_ENDPOINTS = {
                        "/borrow-slips"
        };

        public static final String[] BRANCH_STAFF_GET_ENDPOINTS = {
                        "/borrow-slips/current-branch",
                        "/inventories/current-branch"
        };

        public static final String[] BRANCH_STAFF_PUT_ENDPOINTS = {
                        "/borrow-slips/*/cancel",
                        "/borrow-slips/*/status"
        };

        // =========================================================================
        // 5. ENDPOINTS DÀNH CHO TOÀN BỘ NHÂN SỰ NỘI BỘ (ADMIN, BRANCHMANAGER & STAFF)
        // =========================================================================
        public static final String[] INTERNAL_STAFF_POST_ENDPOINTS = {
                        "/accounts/customer"
        };

        public static final String[] INTERNAL_STAFF_GET_ENDPOINTS = {
                        "/users/customer",
                        "/admin/borrow-slips"
        };

        public static final String[] INTERNAL_STAFF_PUT_ENDPOINTS = {
                        "/users/*"
        };
}
