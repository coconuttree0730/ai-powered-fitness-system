package com.fitness.common.sensitive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SensitiveSerializerTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new SensitiveModule());

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Data
    static class TestVO {
        @Sensitive(SensitiveType.PHONE)
        private String phone;

        @Sensitive(SensitiveType.EMAIL)
        private String email;

        @Sensitive(SensitiveType.NAME)
        private String name;

        @Sensitive(SensitiveType.ADDRESS)
        private String address;
    }

    @Test
    void serialize_noAuth_returnsMasked() throws JsonProcessingException {
        TestVO vo = new TestVO();
        vo.setPhone("13800131234");
        vo.setEmail("zhangsan@qq.com");
        vo.setName("张小明");
        vo.setAddress("北京市朝阳区三里屯太古里");

        String json = mapper.writeValueAsString(vo);

        assertTrue(json.contains("138****1234"));
        assertTrue(json.contains("zha***@qq.com"));
        assertTrue(json.contains("张*明"));
        assertTrue(json.contains("北京市朝阳区***"));
    }

    @Test
    void serialize_adminRole_returnsOriginal() throws JsonProcessingException {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "pass",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        TestVO vo = new TestVO();
        vo.setPhone("13800131234");
        vo.setEmail("zhangsan@qq.com");

        String json = mapper.writeValueAsString(vo);

        assertTrue(json.contains("13800131234"));
        assertTrue(json.contains("zhangsan@qq.com"));
    }

    @Test
    void serialize_coachRole_returnsOriginal() throws JsonProcessingException {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("coach", "pass",
                        List.of(new SimpleGrantedAuthority("ROLE_COACH"))));

        TestVO vo = new TestVO();
        vo.setPhone("13800131234");

        String json = mapper.writeValueAsString(vo);

        assertTrue(json.contains("13800131234"));
    }

    @Test
    void serialize_memberRole_returnsMasked() throws JsonProcessingException {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("member", "pass",
                        List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))));

        TestVO vo = new TestVO();
        vo.setPhone("13800131234");

        String json = mapper.writeValueAsString(vo);

        assertTrue(json.contains("138****1234"));
    }

    @Test
    void serialize_nullField_returnsNull() throws JsonProcessingException {
        TestVO vo = new TestVO();
        vo.setPhone(null);

        String json = mapper.writeValueAsString(vo);

        assertTrue(json.contains("\"phone\":null"));
    }
}
