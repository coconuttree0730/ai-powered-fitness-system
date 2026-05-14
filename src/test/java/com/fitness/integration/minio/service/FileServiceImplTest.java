package com.fitness.integration.minio.service;

import com.fitness.integration.minio.client.MinioStorageClient;
import com.fitness.integration.minio.model.vo.FileUploadVO;
import com.fitness.integration.minio.service.impl.FileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private MinioStorageClient minioStorageClient;

    @Mock
    private FileValidationService fileValidationService;

    @Mock
    private FileMetadataService fileMetadataService;

    @InjectMocks
    private FileServiceImpl fileService;

    @Test
    void uploadImageShouldValidateStoreAndPersistMetadata() {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "png".getBytes());
        when(minioStorageClient.upload(eq(file), anyString())).thenReturn("images/generated.png");
        when(minioStorageClient.getFileUrl("images/generated.png")).thenReturn("http://minio/bucket/images/generated.png");

        FileUploadVO result = fileService.uploadImage(file);

        verify(fileValidationService).validateImage(file);
        verify(fileMetadataService).saveMetadata(eq(file), anyString(), eq("http://minio/bucket/images/generated.png"));
        assertEquals("http://minio/bucket/images/generated.png", result.getFileUrl());
    }
}
