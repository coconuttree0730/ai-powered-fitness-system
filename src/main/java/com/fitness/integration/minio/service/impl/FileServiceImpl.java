package com.fitness.integration.minio.service.impl;

import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.integration.minio.config.MinioProperties;
import com.fitness.integration.minio.mapper.FileMapper;
import com.fitness.integration.minio.model.SysFile;
import com.fitness.integration.minio.model.vo.FileUploadVO;
import com.fitness.integration.minio.service.FileService;
import com.fitness.integration.security.SecurityUtils;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 文件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final FileMapper fileMapper;

    /**
     * 文件大小限制：50MB
     */
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024L;

    /**
     * 允许的图片格式
     */
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    @Override
    public FileUploadVO uploadFile(MultipartFile file, String folder) {
        // 校验文件
        validateFile(file);

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + extension;
        
        // 构建对象名称（folder/filename）
        String objectName = folder.endsWith("/") 
                ? folder + fileName 
                : folder + "/" + fileName;

        try {
            // 上传文件到 MinIO
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            inputStream.close();

            // 获取文件访问URL
            String fileUrl = getFileUrl(objectName);

            // 保存文件记录到数据库
            SysFile sysFile = new SysFile();
            sysFile.setFileName(fileName);
            sysFile.setOriginalName(originalFilename);
            sysFile.setFileUrl(fileUrl);
            sysFile.setFileType(file.getContentType());
            sysFile.setFileSize(file.getSize());
            sysFile.setCreateBy(SecurityUtils.getCurrentUserId());
            sysFile.setCreateTime(LocalDateTime.now());
            fileMapper.insert(sysFile);

            log.info("文件上传成功: {}", fileUrl);

            return FileUploadVO.builder()
                    .fileName(fileName)
                    .fileUrl(fileUrl)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .build();

        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    public FileUploadVO uploadImage(MultipartFile file) {
        // 校验图片类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        return uploadFile(file, "images");
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // 从URL中提取对象名称
            String objectName = extractObjectName(fileUrl);
            if (objectName == null) {
                throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
            }

            // 删除 MinIO 中的文件
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .build()
            );

            // 【逻辑删除】数据库记录（@TableLogic会自动处理）
            SysFile fileRecord = fileMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysFile>()
                            .eq(SysFile::getFileUrl, fileUrl)
            );
            if (fileRecord != null) {
                fileMapper.deleteById(fileRecord.getId());
                log.info("文件记录逻辑删除成功: fileId={}, fileUrl={}", fileRecord.getId(), fileUrl);
            }

            log.info("文件删除成功: {}", fileUrl);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取文件URL失败: {}", e.getMessage(), e);
            // 返回直接访问URL
            return minioProperties.getEndpoint() + "/" + 
                   minioProperties.getBucketName() + "/" + objectName;
        }
    }

    @Override
    public String presignedUploadUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            log.error("生成预签名上传URL失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    /**
     * 校验文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 从URL中提取对象名称
     */
    private String extractObjectName(String fileUrl) {
        if (fileUrl == null) {
            return null;
        }
        
        String bucketName = minioProperties.getBucketName();
        int bucketIndex = fileUrl.indexOf(bucketName);
        if (bucketIndex == -1) {
            return null;
        }
        
        int startIndex = bucketIndex + bucketName.length();
        if (startIndex >= fileUrl.length()) {
            return null;
        }
        
        // 移除开头的斜杠
        String objectName = fileUrl.substring(startIndex);
        if (objectName.startsWith("/")) {
            objectName = objectName.substring(1);
        }
        
        // 如果URL包含查询参数，移除它们
        int queryIndex = objectName.indexOf("?");
        if (queryIndex > 0) {
            objectName = objectName.substring(0, queryIndex);
        }
        
        return objectName;
    }
}
