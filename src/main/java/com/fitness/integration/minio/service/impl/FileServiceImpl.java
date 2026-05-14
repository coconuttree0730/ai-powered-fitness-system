package com.fitness.integration.minio.service.impl;

import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.integration.minio.client.MinioStorageClient;
import com.fitness.integration.minio.model.vo.FileUploadVO;
import com.fitness.integration.minio.service.FileMetadataService;
import com.fitness.integration.minio.service.FileService;
import com.fitness.integration.minio.service.FileValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioStorageClient minioStorageClient;
    private final FileValidationService fileValidationService;
    private final FileMetadataService fileMetadataService;

    @Override
    public FileUploadVO uploadFile(MultipartFile file, String folder) {
        fileValidationService.validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID() + extension;
        String objectName = folder.endsWith("/") ? folder + fileName : folder + "/" + fileName;

        try {
            String storedObjectName = minioStorageClient.upload(file, objectName);
            String fileUrl = minioStorageClient.getFileUrl(storedObjectName);
            fileMetadataService.saveMetadata(file, fileName, fileUrl);

            return FileUploadVO.builder()
                    .fileName(fileName)
                    .fileUrl(fileUrl)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .build();
        } catch (RuntimeException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    public FileUploadVO uploadImage(MultipartFile file) {
        fileValidationService.validateImage(file);
        return uploadFile(file, "images");
    }

    @Override
    public void deleteFile(String fileUrl) {
        String objectName = extractObjectName(fileUrl);
        if (objectName == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        try {
            minioStorageClient.delete(objectName);
            fileMetadataService.deleteByUrl(fileUrl);
        } catch (RuntimeException e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        return minioStorageClient.getFileUrl(objectName);
    }

    @Override
    public String presignedUploadUrl(String objectName) {
        try {
            return minioStorageClient.presignedUploadUrl(objectName);
        } catch (RuntimeException e) {
            log.error("生成预签名上传URL失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String extractObjectName(String fileUrl) {
        if (fileUrl == null) {
            return null;
        }

        int protocolSeparator = fileUrl.indexOf("://");
        int pathStart = protocolSeparator >= 0 ? fileUrl.indexOf('/', protocolSeparator + 3) : fileUrl.indexOf('/');
        if (pathStart < 0 || pathStart + 1 >= fileUrl.length()) {
            return null;
        }

        String path = fileUrl.substring(pathStart + 1);
        int firstSlash = path.indexOf('/');
        if (firstSlash < 0 || firstSlash + 1 >= path.length()) {
            return null;
        }

        String objectName = path.substring(firstSlash + 1);
        int queryIndex = objectName.indexOf('?');
        if (queryIndex > 0) {
            objectName = objectName.substring(0, queryIndex);
        }
        return objectName;
    }
}
