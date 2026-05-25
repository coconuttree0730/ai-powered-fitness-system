package com.fitness.integration.minio.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件上传结果")
public class FileUploadVO {

    /**
     * 存储文件名
     */
    @Schema(description = "存储后的文件名", example = "avatar_1001.png")
    private String fileName;

    /**
     * 文件访问URL
     */
    @Schema(description = "文件访问地址", example = "https://cdn.example.com/files/avatar_1001.png")
    private String fileUrl;

    /**
     * 文件类型
     */
    @Schema(description = "文件 MIME 类型", example = "image/png")
    private String fileType;

    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小，单位字节", example = "204800")
    private Long fileSize;
}
