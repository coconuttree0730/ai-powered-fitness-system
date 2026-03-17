package com.fitness.integration.minio.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.integration.minio.model.SysFile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件记录 Mapper 接口
 */
@Mapper
public interface FileMapper extends BaseMapper<SysFile> {
}
