package com.fitness.common.constants;

public final class CommonConstants {

    private CommonConstants() {
    }

    public static final String UTF_8 = "UTF-8";

    public static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_PAGE_NUM = "1";

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_COACH = "ROLE_COACH";
}
