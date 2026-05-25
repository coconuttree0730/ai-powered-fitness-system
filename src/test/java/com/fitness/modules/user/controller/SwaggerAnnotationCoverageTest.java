package com.fitness.modules.user.controller;

import com.fitness.integration.minio.controller.FileController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SwaggerAnnotationCoverageTest {

    @Test
    void authControllerShouldDeclareChineseTag() {
        Tag tag = AuthController.class.getAnnotation(Tag.class);

        assertNotNull(tag);
        assertEquals("认证管理", tag.name());
    }

    @Test
    void userControllerShouldDeclareChineseTag() {
        Tag tag = UserController.class.getAnnotation(Tag.class);

        assertNotNull(tag);
        assertEquals("用户个人信息", tag.name());
    }

    @Test
    void fileControllerShouldDeclareChineseTag() {
        Tag tag = FileController.class.getAnnotation(Tag.class);

        assertNotNull(tag);
        assertEquals("文件上传管理", tag.name());
    }

    @Test
    void authControllerLoginShouldDeclareOperationSummary() throws Exception {
        Method method = AuthController.class.getMethod("login", com.fitness.modules.user.model.dto.LoginDTO.class);
        Operation operation = method.getAnnotation(Operation.class);

        assertNotNull(operation);
        assertEquals("用户名密码登录", operation.summary());
    }

    @Test
    void userControllerGetCurrentUserInfoShouldDeclareOperationSummary() throws Exception {
        Method method = UserController.class.getMethod("getCurrentUserInfo");
        Operation operation = method.getAnnotation(Operation.class);

        assertNotNull(operation);
        assertEquals("获取当前用户信息", operation.summary());
    }

    @Test
    void fileControllerUploadFileShouldDeclareOperationSummary() throws Exception {
        Method method = FileController.class.getMethod("uploadFile", MultipartFile.class, String.class);
        Operation operation = method.getAnnotation(Operation.class);

        assertNotNull(operation);
        assertEquals("上传通用文件", operation.summary());
    }
}
