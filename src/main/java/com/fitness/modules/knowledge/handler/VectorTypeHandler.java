package com.fitness.modules.knowledge.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(float[].class)
public class VectorTypeHandler extends BaseTypeHandler<float[]> {

    private static Class<?> pgObjectClass;
    private static Constructor<?> pgObjectConstructor;
    private static Method setTypeMethod;
    private static Method setValueMethod;

    static {
        try {
            pgObjectClass = Class.forName("org.postgresql.util.PGobject");
            pgObjectConstructor = pgObjectClass.getConstructor();
            setTypeMethod = pgObjectClass.getMethod("setType", String.class);
            setValueMethod = pgObjectClass.getMethod("setValue", String.class);
        } catch (Exception e) {
            // PGobject class not available, will use string fallback
        }
    }

    private float[] parse(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        
        String vectorStr = json;
        if (json.startsWith("[") && json.endsWith("]")) {
            vectorStr = json.substring(1, json.length() - 1);
        }
        
        String[] parts = vectorStr.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
    }

    private String toVectorString(float[] obj) {
        if (obj == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < obj.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(obj[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, float[] parameter, JdbcType jdbcType) throws SQLException {
        String vectorStr = toVectorString(parameter);
        
        if (pgObjectClass != null && pgObjectConstructor != null) {
            try {
                Object pgObject = pgObjectConstructor.newInstance();
                setTypeMethod.invoke(pgObject, "vector");
                setValueMethod.invoke(pgObject, vectorStr);
                ps.setObject(i, pgObject);
                return;
            } catch (Exception e) {
                // Fall back to string
            }
        }
        
        ps.setString(i, vectorStr);
    }

    @Override
    public float[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return parse(value);
    }

    @Override
    public float[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return parse(value);
    }

    @Override
    public float[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return parse(value);
    }
}
