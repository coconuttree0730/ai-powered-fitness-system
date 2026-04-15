package com.fitness.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.modules.system.model.dto.SysDictDTO;
import com.fitness.modules.system.model.entity.SysDict;
import com.fitness.modules.system.model.vo.SysDictVO;

import java.util.List;
import java.util.Map;

public interface SysDictService extends IService<SysDict> {

    SysDictVO createDict(SysDictDTO dto);

    SysDictVO updateDict(Long id, SysDictDTO dto);

    void deleteDict(Long id);

    SysDictVO getDictById(Long id);

    SysDictVO getDictByCode(String dictCode);

    List<SysDictVO> listAllDicts();

    /**
     * 获取字典选项列表（用于下拉选择）
     */
    List<Map<String, Object>> getOptionsByCode(String dictCode);
}
