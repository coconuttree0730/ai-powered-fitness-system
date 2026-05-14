package com.fitness.integration.minio.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.integration.minio.mapper.FileMapper;
import com.fitness.integration.minio.model.SysFile;
import com.fitness.integration.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FileMetadataService {

    private final FileMapper fileMapper;

    public void saveMetadata(MultipartFile file, String fileName, String fileUrl) {
        SysFile sysFile = new SysFile();
        sysFile.setFileName(fileName);
        sysFile.setOriginalName(file.getOriginalFilename());
        sysFile.setFileUrl(fileUrl);
        sysFile.setFileType(file.getContentType());
        sysFile.setFileSize(file.getSize());
        sysFile.setCreateBy(SecurityUtils.getCurrentUserId());
        sysFile.setCreateTime(LocalDateTime.now());
        fileMapper.insert(sysFile);
    }

    public int deleteByUrl(String fileUrl) {
        return fileMapper.delete(new LambdaQueryWrapper<SysFile>().eq(SysFile::getFileUrl, fileUrl));
    }
}
