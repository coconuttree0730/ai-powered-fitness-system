package com.fitness.integration.minio.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 配置类
 * 配置 MinioClient 并确保存储桶存在
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties minioProperties;

    /**
     * 配置 MinioClient
     */
    @Bean
    public MinioClient minioClient() {
        MinioClient.Builder builder = MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey());
        
        // 如果配置了secure，且endpoint是http/https格式，则根据secure设置
        // MinIO SDK 会自动从endpoint解析协议
        MinioClient minioClient = builder.build();

        // 确保存储桶存在
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .build()
            );
            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioProperties.getBucketName())
                                .build()
                );
                log.info("MinIO 存储桶 '{}' 创建成功", minioProperties.getBucketName());
            } else {
                log.info("MinIO 存储桶 '{}' 已存在", minioProperties.getBucketName());
            }
        } catch (Exception e) {
            log.error("MinIO 存储桶检查/创建失败: {}", e.getMessage(), e);
        }

        return minioClient;
    }
}
