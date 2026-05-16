package com.fitness.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.system.model.entity.SysDictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysDictItemMapper extends BaseMapper<SysDictItem> {

    @Select("SELECT * FROM sys_dict_item WHERE dict_id = #{dictId} AND deleted = false ORDER BY sort_order")
    List<SysDictItem> selectByDictId(Long dictId);
}
