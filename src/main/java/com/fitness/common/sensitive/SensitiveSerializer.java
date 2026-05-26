package com.fitness.common.sensitive;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * 敏感数据序列化器
 * 根据 @Sensitive 注解的类型和当前用户角色决定是否脱敏
 */
public class SensitiveSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private static final Set<String> FULL_ACCESS_ROLES = Set.of("ROLE_ADMIN", "ROLE_COACH");

    private final SensitiveType type;

    public SensitiveSerializer() {
        this.type = null;
    }

    public SensitiveSerializer(SensitiveType type) {
        this.type = type;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (type != null && shouldMask()) {
            gen.writeString(MaskUtil.mask(value, type));
        } else {
            gen.writeString(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
            throws JsonMappingException {
        if (property != null) {
            Sensitive annotation = property.getAnnotation(Sensitive.class);
            if (annotation != null) {
                return new SensitiveSerializer(annotation.value());
            }
        }
        return this;
    }

    private boolean shouldMask() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return true;
        }
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .noneMatch(FULL_ACCESS_ROLES::contains);
    }
}
