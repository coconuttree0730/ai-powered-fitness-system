package com.fitness.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.system.model.entity.SysDictItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysDictItemMapper extends BaseMapper<SysDictItem> {

    List<SysDictItem> selectByDictId(Long dictId);
}
