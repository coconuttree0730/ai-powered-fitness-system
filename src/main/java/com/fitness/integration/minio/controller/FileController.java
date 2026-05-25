package com.fitness.integration.minio.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.minio.model.vo.FileUploadVO;
import com.fitness.integration.minio.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理 Controller
 * 基础路径：/api/v1/files
 */
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "文件上传管理", description = "通用文件、图片上传与文件删除接口")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileService fileService;

    /**
     * 上传文件
     *
     * @param file   上传的文件
     * @param folder 存储文件夹（可选，默认为 files）
     * @return 文件上传响应
     */
    @Operation(summary = "上传通用文件", description = "上传任意业务文件到对象存储，并返回文件访问地址")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<FileUploadVO> uploadFile(
            @Parameter(description = "待上传的文件")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "对象存储目录，默认值为 files", example = "files")
            @RequestParam(value = "folder", defaultValue = "files") String folder) {
        FileUploadVO result = fileService.uploadFile(file, folder);
        return Result.success(result);
    }

    /**
     * 上传图片
     *
     * @param file 上传的图片文件
     * @return 文件上传响应
     */
    @Operation(summary = "上传图片文件", description = "上传图片资源到对象存储，并返回图片访问地址")
    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<FileUploadVO> uploadImage(
            @Parameter(description = "待上传的图片文件")
            @RequestParam("file") MultipartFile file) {
        FileUploadVO result = fileService.uploadImage(file);
        return Result.success(result);
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件访问URL
     * @return 成功响应
     */
    @Operation(summary = "删除文件", description = "根据文件访问地址删除对象存储中的文件")
    @DeleteMapping
    public Result<Void> deleteFile(
            @Parameter(description = "待删除文件的完整访问地址")
            @RequestParam("fileUrl") String fileUrl) {
        fileService.deleteFile(fileUrl);
        return Result.success();
    }
}
