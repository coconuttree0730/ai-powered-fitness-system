package com.fitness.integration.minio.config;

import lombok.Data;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 配置属性
 * 从 application.yml 读取 minio 前缀的配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * MinIO 服务地址
     */
    private String endpoint = "http://localhost:9000";

    /**
     * 访问密钥
     */
    private String accessKey = "minioadmin";

    /**
     * 私有密钥
     */
    private String secretKey = "minioadmin";

    /**
     * 存储桶名称
     */
    private String bucketName = "fitness-bucket";

    /**
     * 是否使用 HTTPS
     */
    private Boolean secure = false;
}
