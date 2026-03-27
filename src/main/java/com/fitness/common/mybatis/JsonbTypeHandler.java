package com.fitness.common.mybatis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PostgreSQL jsonb 类型处理器 *****
 * 用于处理 Java Object 与 PostgreSQL jsonb 类型之间的转换
 */
@MappedJdbcTypes(JdbcType.OTHER)
@MappedTypes(Object.class)
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
        return rs.getObject(columnName);
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getObject(columnIndex);
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getObject(columnIndex);
    }
}
