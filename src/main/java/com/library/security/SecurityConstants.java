package com.library.security;

public final class SecurityConstants {

    private SecurityConstants() {
        // Utility class
    }

    public static final String[] PUBLIC_ENDPOINTS = {
            "/auth/**",
            "/hello/**",
            "/error",
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
}
