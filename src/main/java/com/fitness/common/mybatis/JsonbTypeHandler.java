package com.fitness.common.mybatis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * PostgreSQL jsonb 类型处理器
 * 用于处理 Java Object 与 PostgreSQL jsonb 类型之间的转换
 */
@MappedJdbcTypes(JdbcType.OTHER)
@MappedTypes({Object.class, List.class})
public class JsonbTypeHandler extends BaseTypeHandler<Object> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        try {
            String jsonValue;
            if (parameter instanceof String) {
                jsonValue = (String) parameter;
            } else {
                jsonValue = objectMapper.writeValueAsString(parameter);
            }
            // 使用 JDBC 标准的 setObject 方法，让驱动处理 jsonb 类型
            ps.setObject(i, jsonValue, java.sql.Types.OTHER);
        } catch (Exception e) {
            throw new SQLException("Error converting Object to jsonb", e);
        }
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJsonb(rs.getObject(columnName));
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJsonb(rs.getObject(columnIndex));
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJsonb(cs.getObject(columnIndex));
    }

    /**
     * 解析 PostgreSQL jsonb 对象为 Java 对象
     */
    private Object parseJsonb(Object value) {
        if (value == null) {
            return null;
        }

        String json = extractJsonFromPgObject(value);
        if (json != null) {
            if (json.isEmpty()) {
                return null;
            }
            try {
                if (json.startsWith("[")) {
                    return objectMapper.readValue(json, new TypeReference<List<Object>>() {});
                }
                if (json.startsWith("{")) {
                    return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
                }
                return json;
            } catch (Exception e) {
                return json;
            }
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.startsWith("[")) {
                try {
                    return objectMapper.readValue(str, new TypeReference<List<Object>>() {});
                } catch (Exception e) {
                    return str;
                }
            }
            if (str.startsWith("{")) {
                try {
                    return objectMapper.readValue(str, new TypeReference<Map<String, Object>>() {});
                } catch (Exception e) {
                    return str;
                }
            }
            return str;
        }

        return value;
    }
    
    /**
     * 使用反射从 PGobject 中提取 JSON 字符串
     */
    private String extractJsonFromPgObject(Object value) {
        try {
            // 检查是否是 PGobject 类型（类名匹配）
            if (value.getClass().getName().equals("org.postgresql.util.PGobject")) {
                // 通过反射调用 getValue() 方法
                java.lang.reflect.Method getValueMethod = value.getClass().getMethod("getValue");
                return (String) getValueMethod.invoke(value);
            }
        } catch (Exception e) {
            // 反射失败，忽略
        }
        return null;
    }
}
