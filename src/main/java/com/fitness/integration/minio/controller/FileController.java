package com.fitness.integration.minio.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.minio.model.vo.FileUploadVO;
import com.fitness.integration.minio.service.FileService;
import lombok.RequiredArgsConstructor;
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
public class FileController {

    private final FileService fileService;

    /**
     * 上传文件
     *
     * @param file   上传的文件
     * @param folder 存储文件夹（可选，默认为 files）
     * @return 文件上传响应
     */
    @PostMapping("/upload")
    public Result<FileUploadVO> uploadFile(
            @RequestParam("file") MultipartFile file,
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
    @PostMapping("/upload/image")
    public Result<FileUploadVO> uploadImage(@RequestParam("file") MultipartFile file) {
        FileUploadVO result = fileService.uploadImage(file);
        return Result.success(result);
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件访问URL
     * @return 成功响应
     */
    @DeleteMapping
    public Result<Void> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        fileService.deleteFile(fileUrl);
        return Result.success();
    }
}
