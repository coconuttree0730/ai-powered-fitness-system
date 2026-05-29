package com.fitness.config.security;

public final class SecurityWhitelist {

    private SecurityWhitelist() {
    }

    public static final String[] URLS = {
            "/api/v1/auth/login",
            "/api/v1/auth/login/sms",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/v1/auth/slider-verify/**",
            "/api/v1/auth/sms-code",
            "/api/v1/courses/public/**",
            "/api/v1/courses/homepage/**",
            "/api/v1/banners/active",
            "/api/v1/announcements/published",
            "/api/v1/coaches/home",
            "/api/v1/coaches/*/detail",
            "/api/v1/equipment/**",
            "/api/v1/membership/cards/**",
            "/static/**",
            "/uploads/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/error",
            "/favicon.ico",
            "/api/v1/payment/alipay/notify",
            "/api/v1/payment/product-order/alipay/notify"
    };
}
