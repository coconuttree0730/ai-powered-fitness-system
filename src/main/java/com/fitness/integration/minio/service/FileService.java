package com.fitness.integration.minio.service;

import com.fitness.integration.minio.model.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传文件
     *
     * @param file   上传的文件
     * @param folder 存储文件夹（如 avatars, equipment, courses）
     * @return 文件上传响应
     */
    FileUploadVO uploadFile(MultipartFile file, String folder);

    /**
     * 上传图片（校验文件类型）
     *
     * @param file 上传的图片文件
     * @return 文件上传响应
     */
    FileUploadVO uploadImage(MultipartFile file);

    /**
     * 删除文件
     *
     * @param fileUrl 文件访问URL
     */
    void deleteFile(String fileUrl);

    /**
     * 获取文件访问URL
     *
     * @param objectName 对象名称（文件路径）
     * @return 文件访问URL
     */
    String getFileUrl(String objectName);

    /**
     * 生成预签名上传URL（可选功能）
     *
     * @param objectName 对象名称（文件路径）
     * @return 预签名上传URL
     */
    String presignedUploadUrl(String objectName);
}
