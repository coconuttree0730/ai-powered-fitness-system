package com.fitness.common.cache;

public final class RedisCacheNames {

    public static final String ACTIVE_BANNERS = "banner:active";
    public static final String PUBLISHED_ANNOUNCEMENTS = "announcement:published";
    public static final String DICT_OPTIONS = "dict:options";
    public static final String COURSE_PUBLIC_LIST = "course:public:list";
    public static final String COURSE_CATEGORIES = "course:categories";
    public static final String COURSE_HOME_CATEGORIES = "course:homepage:categories";
    public static final String COURSE_HOME_CARDS = "course:homepage:cards";
    public static final String UPCOMING_SESSIONS = "course:session:upcoming";
    public static final String COACH_HOME = "coach:home";
    public static final String PRODUCT_PUBLIC_LIST = "product:public:list";
    public static final String MEMBERSHIP_ACTIVE_CARDS = "membership:active:cards";
    public static final String MEMBERSHIP_RECOMMEND_CARDS = "membership:recommend:cards";
    public static final String USER_PERMISSIONS = "user:permissions";
    public static final String USER_ROLES = "user:roles";

    private RedisCacheNames() {
    }
}
