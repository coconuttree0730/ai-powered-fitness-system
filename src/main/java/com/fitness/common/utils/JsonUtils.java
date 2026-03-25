package com.fitness.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON工具类
 * 提供JSON相关的工具方法，如对象转JSON、JSON转对象等
 */
@Slf4j
public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private JsonUtils() {
    }

    /**
     * 将对象转换  --> 为JSON字符串
     * @param obj 要转换的对象
     * @return 对象的JSON字符串表示
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            // 转换为JSON字符串
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象转JSON失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将对象转换  --> 为JSON字符串（格式化）
     * @param obj 要转换的对象
     * @return 对象的JSON字符串表示（格式化）
     */
    public static String toJsonPretty(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            // 转换为JSON字符串（格式化）
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象转JSON失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将JSON字符串转换  --> 为对象
     * @param json 要转换的JSON字符串
     * @param clazz 目标对象的类
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON转对象失败: {}", e.getMessage());
            return null;
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("JSON转对象失败: {}", e.getMessage());
            return null;
        }
    }

     /**
     * 获取默认的ObjectMapper实例
     * @return 默认的ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}
